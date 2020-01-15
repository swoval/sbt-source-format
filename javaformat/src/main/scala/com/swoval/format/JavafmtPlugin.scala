package com.swoval.format

import com.swoval.format.java.JavaFormatter
import com.swoval.format.lib.SourceFormat
import _root_.java.nio.file.Path
import sbt._

object JavafmtPlugin extends AutoPlugin with JavafmtKeys {
  override def trigger = allRequirements
  object autoImport extends JavafmtKeys
  private val javaFormatter = TaskKey[(Path, Logger) => String]("java-formatter", "", Int.MaxValue)

  override lazy val projectSettings = Def.settings(
    javaFormatter := JavaFormatter,
    SourceFormat.settings(
      autoImport.javafmt,
      javaFormatter,
      SourceFormat.compileSources(Compile, "*.java"),
      SourceFormat.compileSources(Test, "*.java"),
    ),
  )
}

private[format] trait JavafmtKeys {
  val javafmt = taskKey[Unit]("Format source files using the google java formatter.")
  val javafmtCheck =
    taskKey[Unit]("Check source file formatting using the google java formatter.")
}
