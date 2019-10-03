package com.swoval.format

import java.net.URLClassLoader
import java.nio.file.{ Files, Path }

import com.swoval.format.impl.ClangFormatter
import sbt.Keys._
import sbt._
import sbt.nio.{ FileStamp, FileStamper }
import sbt.nio.Keys.{ fileInputs, inputFileStamps, outputFileStamper, outputFileStamps }

import scala.util.Try

/**
 * An sbt plugin that provides sources formatting tasks. The default tasks can either format the
 * task in place, or verify the source path formatting by adding the --check flag to the task key.
 */
object SourceFormatPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    private def checkMessage(formatter: String) =
      s"Validate that all source files are correctly formatted according to $formatter."
    val clangfmt = taskKey[Unit]("Format source files using clang format.")
    val clangfmtCheck = taskKey[Unit](checkMessage("clang-format"))
    val javafmt = taskKey[Unit]("Format source files using the google java formatter")
    val javafmtCheck = taskKey[Unit](checkMessage("javafmt"))
  }

  private val clangFormatted = taskKey[Seq[Path]]("Implements formatting")
  private val javaFormatted = taskKey[Seq[Path]]("Implements formatting")
  import autoImport._
  private val javaFormatter: Path => String = {
    val loader = this.getClass.getClassLoader match {
      case l: URLClassLoader =>
        val sorted = l.getURLs.toSeq.sortBy(u => if (u.toString.contains("guava")) -1 else 1)
        new URLClassLoader(sorted.toArray, l.getParent)
      case l => l
    }
    loader.loadClass("com.swoval.format.impl.JavaFormatter$").getDeclaredField("MODULE$").get(null)
  }.asInstanceOf[Path => String]
  private val SourceFormatOverwrite = AttributeKey[Boolean]("run-format")
  private def formatted(
      self: TaskKey[Seq[Path]],
      key: TaskKey[Unit],
      formatter: Path => String,
      formatterName: String
  ): Seq[Def.Setting[_]] =
    (self := Def
      .taskDyn[Seq[Path]] {
        val inputStamps = getInputStamps(key).value
        val prev = (key / outputFileStamps).previous.getOrElse(Nil).toMap
        val (formatted, needFormat) = inputStamps.partition {
          case (p, s) => prev.get(p).contains(s)
        }
        val logger = streams.value.log
        val n = name.value
        val overwrite = state.value.get(SourceFormatOverwrite).getOrElse(false)
        val prefix = if (overwrite) "Formatting" else "Checking formatting for"
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
              val formatted = formatter(path)
              if (previous == formatted) Some(path)
              else if (overwrite) Try(Files.write(path, formatted.getBytes)).toOption
              else None
            } catch { case _: Exception => None }
          } { t =>
            t.copy(info = t.info.setName(s"$formatterName:${base.relativize(path)}"))
          }
        needFormat.map { case (p, _) => task(p) }.join.flatMap { formatTasks =>
          joinTasks(formatTasks).join.map(p => formatted.map(_._1) ++ p.flatten)
        }
      }
      .value) :: (self / outputFileStamper := FileStamper.Hash) ::
      ((key / outputFileStamps) := (self / outputFileStamps).triggeredBy(self).value) :: Nil

  private def formatImpl(
      key: TaskKey[Unit],
      implKey: TaskKey[Seq[Path]],
      overwrite: Boolean,
      ex: Set[Path] => UnformattedFilesException
  ): Def.Initialize[Task[Unit]] = Def.task {
    val impl = thisProjectRef.value / implKey
    val st = state.value
    val formatted =
      Project.extract(st).runTask(impl, st.put(SourceFormatOverwrite, overwrite))._2.toSet
    val all = key.inputFiles.toSet
    val diff = all diff formatted
    if (diff.nonEmpty) throw ex(diff)
  }
  private def getInputStamps(key: TaskKey[_]): Def.Initialize[Task[Seq[(Path, FileStamp)]]] =
    Def.taskDyn {
      val scoped = resolvedScoped.value.scope.project
      (key in key.scope.copy(project = scoped)) / inputFileStamps
    }
  private val javaError: Set[Path] => UnformattedFilesException = paths =>
    UnformattedFilesException.Java(paths.toSeq: _*)
  private val clangError: Set[Path] => UnformattedFilesException = paths =>
    UnformattedFilesException.Clang(paths.toSeq: _*)
  private def javaSettings(config: Configuration): Seq[Def.Setting[_]] = Def.settings(
    config / javaFormatted / outputFileStamper := FileStamper.Hash,
    config / javafmt / SourceFormatWrappers.unmanagedFileStampCache := SourceFormatWrappers.NoCache,
    config / javafmt / fileInputs := {
      val allDirs = (config / unmanagedSourceDirectories).value
      allDirs.map(_.toGlob / ** / "*.java")
    },
    formatted(config / javaFormatted, config / javafmt, javaFormatter, formatterName = "javafmt"),
    (config / javafmt) :=
      formatImpl(
        config / javafmt,
        config / javaFormatted,
        overwrite = true,
        ex = javaError
      ).value,
    (config / javafmtCheck) :=
      formatImpl(
        config / javafmt,
        config / javaFormatted,
        overwrite = false,
        ex = javaError
      ).value
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    clangfmt / SourceFormatWrappers.unmanagedFileStampCache := SourceFormatWrappers.NoCache,
    clangFormatted / outputFileStamper := FileStamper.Hash,
    formatted(clangFormatted, clangfmt, ClangFormatter, formatterName = "clangfmt"),
    clangfmt := formatImpl(clangfmt, clangFormatted, overwrite = true, clangError).value,
    clangfmtCheck := formatImpl(clangfmt, clangFormatted, overwrite = false, clangError).value,
    javaSettings(Compile),
    javaSettings(Test),
    javafmt := {
      (Compile / javafmt).value
      (Test / javafmt).value
    },
    javafmtCheck := (Test / javafmtCheck).value
  )
}
