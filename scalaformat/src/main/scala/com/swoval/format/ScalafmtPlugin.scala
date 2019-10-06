package com.swoval.format

import java.nio.file.{ Files, Path }

import com.swoval.format.lib.SourceFormat
import com.swoval.format.scala.ScalaFormatter
import sbt.Keys.baseDirectory
import sbt._

object ScalafmtPlugin extends AutoPlugin {
  override def trigger = allRequirements
  trait Keys {
    val scalafmt = taskKey[Unit]("Format source files using scalafmt.")
    val scalafmtCheck = taskKey[Unit]("Check source file formatting using scalafmt.")
    val scalafmtConfig = taskKey[Path]("The scalafmt config file.")
  }
  object autoImport extends Keys
  import autoImport._
  private val checkConfig = TaskKey[Unit]("scalafmtCheckConfig", "", Int.MaxValue)
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def
    .settings(
      SourceFormat.settings(
        scalafmt,
        ScalaFormatter,
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
      checkConfig := {
        Files.readAllBytes(scalafmtConfig.value)
        ()
      },
      (Compile / scalafmt) := (Compile / scalafmt).dependsOn(checkConfig).value,
      (Test / scalafmt) := (Test / scalafmt).dependsOn(checkConfig).value
    )
}
