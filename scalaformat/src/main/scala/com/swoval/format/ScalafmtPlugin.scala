package com.swoval.format

import java.nio.file.{ Files, Path }

import com.swoval.format.lib.SourceFormat
import sbt.Keys.baseDirectory
import sbt._

object ScalafmtPlugin extends AutoPlugin with ScalafmtKeys {
  override def trigger = allRequirements
  object autoImport extends ScalafmtKeys
  private val configContents = TaskKey[String]("scalafmt-config-contents", "", Int.MaxValue)
  private val formatter =
    TaskKey[(Path, Path, Logger) => String]("scalafmt-formatter", "", Int.MaxValue)
  private val scalafmtVersion = "2.3.2"
  private val Version = "version[ ]*=[ ]*(.*)".r
  override lazy val globalSettings: Seq[Def.Setting[_]] = Def.settings(
    scalafmtCoursierCachePath := None,
  )
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
        SourceFormat.compileSources(Compile, "*.{scala,sbt,sc}"),
        SourceFormat.compileSources(Test, "*.{scala,sbt,sc}"),
      ),
      configContents := new String(Files.readAllBytes(scalafmtConfig.value)),
      (Compile / scalafmt) := (Compile / scalafmt).dependsOn(configContents).value,
      (Test / scalafmt) := (Test / scalafmt).dependsOn(configContents).value
    )
}

private[format] trait ScalafmtKeys {
  val scalafmt = taskKey[Unit]("Format source files using scalafmt.")
  val scalafmtCheck = taskKey[Unit]("Check source file formatting using scalafmt.")
  val scalafmtConfig = taskKey[Path]("The scalafmt config file.")
  val scalafmtCoursierCachePath = settingKey[Option[Path]]("The coursier cache path for scalafmt.")
}
