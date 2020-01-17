package com.swoval.format

import _root_.java.nio.file.Path

import com.swoval.format.java.JavaFormatter
import com.swoval.format.lib.SourceFormat
import com.swoval.format.lib.SourceFormat.projectSbtBuild
import sbt.Keys.{ baseDirectory, concurrentRestrictions, unmanagedSources }
import sbt._
import sbt.nio.Keys.inputFileStamps

object JavafmtPlugin extends AutoPlugin with JavafmtKeys with lib.Keys {
  override def trigger = allRequirements
  object autoImport extends JavafmtKeys
  private val javaFormatter = TaskKey[(Path, Logger) => String]("java-formatter", "", Int.MaxValue)

  private def javafmtOnCompileImpl(config: ConfigKey): Def.Initialize[Task[Unit]] =
    Def.taskDyn(
      if ((javafmtOnCompile in config).?.value.getOrElse(false)) javafmt in config else Def.task(())
    )
  override lazy val globalSettings = Def.settings(
    Global / concurrentRestrictions += Tags
      .limit(ConcurrentRestrictions.Tag(javafmt.key.label), (javafmt / formatTaskLimit).value),
  )
  override lazy val projectSettings = Def.settings(
    javaFormatter := JavaFormatter,
    SourceFormat.settings(
      autoImport.javafmt,
      javaFormatter,
      SourceFormat.compileSources(Compile, "*.java"),
      SourceFormat.compileSources(Test, "*.java"),
      projectSbtBuild -> baseDirectory(d =>
        Seq(d.toGlob / "*.java", d.toGlob / "project" / ** / "*.java")
      ),
    ),
    (Compile / unmanagedSources / inputFileStamps) := (Compile / unmanagedSources / inputFileStamps)
      .dependsOn(javafmtOnCompileImpl(Compile))
      .value,
    (Test / unmanagedSources / inputFileStamps) := (Test / unmanagedSources / inputFileStamps)
      .dependsOn(javafmtOnCompileImpl(Test))
      .value,
    javafmtAll := Seq(Compile, Test).map(_ / javafmt).join.value,
    javafmtCheckAll := Seq(Compile, Test, projectSbtBuild).map(_ / javafmtCheck).join.value,
    javafmtSbt := (projectSbtBuild / javafmt).value,
  )
}

private[format] trait JavafmtKeys {
  val javafmt = taskKey[Unit]("Format source files using the google java formatter.")
  val javafmtSbt = taskKey[Unit]("Format java source files in the sbt build.")
  val javafmtAll = taskKey[Unit]("Format all project source files using the google java formatter.")
  val javafmtCheck =
    taskKey[Unit]("Check source file formatting using the google java formatter.")
  val javafmtCheckAll =
    taskKey[Unit]("Check all project source file formatting using the google java formatter.")
  val javafmtOnCompile =
    settingKey[Boolean]("Toggles whether to perform formatting before compilation.")
}
