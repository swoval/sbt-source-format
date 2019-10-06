package com.swoval.format.lib

import java.nio.file.{ Files, Path, Paths }

import sbt.Keys._
import sbt._
import sbt.nio.Keys._
import sbt.nio._

import scala.util.Try

/**
 * An sbt plugin that provides compileSources formatting tasks. The default tasks can either format the
 * task in place, or verify the source path formatting by adding the --check flag to the task key.
 */
object SourceFormat {
  private val SourceFormatOverwrite = AttributeKey[Boolean]("source-format-overwrite")
  private[format] def formatted(
      self: TaskKey[Seq[Path]],
      key: TaskKey[Unit],
      configKey: TaskKey[Path],
      formatter: (Path, Path, Logger) => String,
      formatterName: String
  ): Seq[Def.Setting[_]] =
    (self := Def
      .taskDyn[Seq[Path]] {
        val inputStamps = getInputStamps(key).value
        val prev = (key / outputFileStamps).previous.getOrElse(Nil).toMap
        val forceReformat = configKey.outputFileChanges.hasChanges
        val configFile = configKey.value
        val (formatted, needFormat) = inputStamps.partition {
          case (p, s) => prev.get(p).contains(s) && !forceReformat
        }
        val logger = streams.value.log
        val overwrite = state.value.get(SourceFormatOverwrite).getOrElse(false)
        val prefix = if (overwrite) "Formatting" else "Checking formatting for"
        val project = resolvedScoped.value.scope.project.toOption.map {
          case p: ProjectRef => p.project
          case _             => ""
        } getOrElse ""
        val conf = key.scope.config.toOption.map {
          case ConfigKey(name) => name.head.toUpper + name.tail
          case _               => ""
        } getOrElse ""
        val n = s"$project${if (project.nonEmpty && conf.nonEmpty) " / " else ""}$conf"
        needFormat.length match {
          case i if i <= 0 =>
          case 1           => logger.info(s"$prefix 1 source in $n using $formatterName")
          case i           => logger.info(s"$prefix $i sources in $n using $formatterName")
        }
        val base = (LocalRootProject / baseDirectory).value.toPath
        val task = (path: Path) =>
          Def.task {
            try {
              val previous = new String(Files.readAllBytes(path))
              val formatted = formatter(configFile, path, logger)
              if (previous == formatted) Some(path)
              else if (overwrite) Try(Files.write(path, formatted.getBytes)).toOption
              else None
            } catch {
              case e: Exception =>
                logger.error(e.toString);
                None
            }
          } { t =>
            t.copy(info = t.info.setName(s"$formatterName:${base.relativize(path)}"))
          }
        needFormat.map { case (p, _) => task(p) }.join.flatMap { formatTasks =>
          joinTasks(formatTasks).join.map(p => formatted.map(_._1) ++ p.flatten)
        }
      }
      .value) :: (self / outputFileStamper := FileStamper.Hash) ::
      ((key / outputFileStamps) := (self / outputFileStamps).triggeredBy(self).value) :: Nil

  private[format] def formatImpl(
      key: TaskKey[Unit],
      implKey: TaskKey[Seq[Path]],
      overwrite: Boolean,
      formatterName: String
  ): Def.Initialize[Task[Unit]] = Def.task {
    val impl = thisProjectRef.value / implKey
    val st = state.value
    val formatted =
      Project.extract(st).runTask(impl, st.put(SourceFormatOverwrite, overwrite))._2.toSet
    val all = key.inputFiles.toSet
    val diff = all diff formatted
    if (diff.nonEmpty) throw new UnformattedFilesException(formatterName, diff.toSeq.sorted)
  }
  private def getInputStamps(key: TaskKey[_]): Def.Initialize[Task[Seq[(Path, FileStamp)]]] =
    Def.taskDyn {
      val scoped = resolvedScoped.value.scope.project
      (key in key.scope.copy(project = scoped)) / inputFileStamps
    }

  private[format] def settings(
      key: TaskKey[Unit],
      formatter: (Path, Path, Logger) => String,
      config: Def.Initialize[Path],
      configs: (Configuration, Def.Initialize[Seq[Glob]])*
  ): Seq[Def.Setting[_]] = {
    configs.flatMap {
      case (conf, inputs) =>
        settings(ThisScope in (conf: ConfigKey), key, formatter, inputs, config)
    }
  }
  private[format] def settings(
      key: TaskKey[Unit],
      formatter: (Path, Path, Logger) => String,
      configs: (Configuration, Def.Initialize[Seq[Glob]])*
  ): Seq[Def.Setting[_]] = {
    configs.flatMap {
      case (conf, inputs) =>
        settings(ThisScope in (conf: ConfigKey), key, formatter, inputs, Def.setting(Paths.get("")))
    }
  }
  private[format] def settings(
      key: TaskKey[Unit],
      formatter: (Path, Path, Logger) => String,
      inputs: Def.Initialize[Seq[Glob]],
      config: Def.Initialize[Path]
  ): Seq[Def.Setting[_]] = {
    settings(ThisScope, key, formatter, inputs, config)
  }
  private[format] def settings(
      key: TaskKey[Unit],
      formatter: (Path, Path, Logger) => String,
      inputs: Def.Initialize[Seq[Glob]]
  ): Seq[Def.Setting[_]] = {
    settings(ThisScope, key, formatter, inputs, Def.setting(Paths.get("")))
  }
  private def settings(
      scope: Scope,
      key: TaskKey[Unit],
      formatter: (Path, Path, Logger) => String,
      inputs: Def.Initialize[Seq[Glob]],
      config: Def.Initialize[Path]
  ): Seq[Def.Setting[_]] = {
    val name = key.key.label
    val implKey =
      TaskKey[Seq[Path]](name + "Impl", "Implement formatting", Int.MaxValue) in key.scope
    val checkKey =
      TaskKey[Unit](name + "Check", "Check formatting", Int.MaxValue) in key.scope
    val configKey =
      TaskKey[Path](name + "Config", "Check formatting", Int.MaxValue) in key.scope
    Def.settings(
      scope / implKey / outputFileStamper := FileStamper.Hash,
      scope / key / SourceFormatWrappers.unmanagedFileStampCache := SourceFormatWrappers.NoCache,
      scope / key / fileInputs := inputs.value,
      configKey := config.value,
      formatted(
        scope / implKey,
        scope / key,
        configKey,
        formatter,
        formatterName = name
      ),
      (scope / key) :=
        formatImpl(
          scope / key,
          scope / implKey,
          overwrite = true,
          name
        ).value,
      (scope / checkKey) :=
        formatImpl(
          scope / key,
          scope / implKey,
          overwrite = false,
          name
        ).value
    )
  }

  def compileSources(
      config: Configuration,
      filter: String
  ): (Configuration, Def.Initialize[Seq[Glob]]) =
    config -> Def.setting((config / unmanagedSourceDirectories).value.map(_.toGlob / ** / filter))
}
