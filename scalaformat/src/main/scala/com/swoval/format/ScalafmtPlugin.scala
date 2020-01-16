package com.swoval.format

import java.nio.file.{ Files, Path }

import com.swoval.format.lib.SourceFormat
import sbt.Keys.{ baseDirectory, concurrentRestrictions, unmanagedSources }
import sbt._
import sbt.nio.Keys.inputFileStamps

object ScalafmtPlugin extends AutoPlugin with ScalafmtKeys with lib.Keys {
  override def trigger = allRequirements
  object autoImport extends ScalafmtKeys
  private val configContents = TaskKey[String]("scalafmt-config-contents", "", Int.MaxValue)
  private val formatter =
    TaskKey[(Path, Path, Logger) => String]("scalafmt-formatter", "", Int.MaxValue)
  private val scalafmtVersion = "2.3.2"
  private val Version = "version[ ]*=[ ]*(.*)".r
  private def scalafmtOnCompileImpl(config: ConfigKey): Def.Initialize[Task[Unit]] =
    Def.taskDyn(
      if ((scalafmtOnCompile in config).?.value.getOrElse(false)) scalafmt in config
      else Def.task(())
    )
  private lazy val swovalBuild = Configuration.of("ProjectSbtBuild", "projectSbtBuild")
  override lazy val globalSettings: Seq[Def.Setting[_]] = Def.settings(
    Global / concurrentRestrictions += Tags
      .limit(ConcurrentRestrictions.Tag(scalafmt.key.label), (scalafmt / formatTaskLimit).value),
    scalafmtCoursierCachePath := None,
  )
  private val filter = "*.{scala,sbt,sc}"
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def
    .settings(
      formatter := {
        val version = configContents.value.linesIterator
          .collectFirst { case Version(v) => v }
          .getOrElse(scalafmtVersion)
        ScalafmtClassLoader(version, scalafmtCoursierCachePath.value)
      },
      SourceFormat.settings(
        scalafmt,
        formatter,
        Def.setting {
          val baseDir = baseDirectory.value.toPath
          val rootDir = (LocalRootProject / baseDirectory).value.toPath
          val base = baseDir / ".scalafmt.conf"
          if (Files.exists(base)) base
          else rootDir / ".scalafmt.conf"
        },
        SourceFormat.compileSources(Compile, filter),
        SourceFormat.compileSources(Test, filter),
        swovalBuild -> baseDirectory(d => Seq(d.toGlob / filter, d.toGlob / "project" / ** / filter)
        )
      ),
      (Compile / unmanagedSources / inputFileStamps) := (Compile / unmanagedSources / inputFileStamps)
        .dependsOn(scalafmtOnCompileImpl(Compile))
        .value,
      (Test / unmanagedSources / inputFileStamps) := (Test / unmanagedSources / inputFileStamps)
        .dependsOn(scalafmtOnCompileImpl(Test))
        .value,
      configContents := new String(Files.readAllBytes(scalafmtConfig.value)),
      (Compile / scalafmt) := (Compile / scalafmt).dependsOn(configContents).value,
      (Test / scalafmt) := (Test / scalafmt).dependsOn(configContents).value,
      (swovalBuild / scalafmt) := (swovalBuild / scalafmt).dependsOn(configContents).value,
      scalafmtSbt := (swovalBuild / scalafmt).value,
      scalafmtSbtCheck := (swovalBuild / scalafmtCheck).value,
      scalafmtAll := Seq(Compile, Test, swovalBuild).map(_ / scalafmt).join.value,
      scalafmtCheckAll := Seq(Compile, Test, swovalBuild).map(_ / scalafmtCheck).join.value
    )
}

private[format] trait ScalafmtKeys {
  final val scalafmt = taskKey[Unit]("Format source files using scalafmt.")
  final val scalafmtAll =
    taskKey[Unit]("Format all project source files, include sbt build sources, using scalafmt.")
  final val scalafmtSbt = taskKey[Unit]("Format build sources")
  final val scalafmtSbtCheck = taskKey[Unit]("Check formatting of build sources")
  final val scalafmtCheck = taskKey[Unit]("Check source file formatting using scalafmt.")
  final val scalafmtCheckAll = taskKey[Unit]("Check source file formatting using scalafmt.")
  final val scalafmtOnCompile =
    settingKey[Boolean]("Toggles whether to perform formatting before compilation.")
  final val scalafmtConfig = taskKey[Path]("The scalafmt config file.")
  final val scalafmtCoursierCachePath =
    settingKey[Option[Path]]("The coursier cache path for scalafmt.")
}
