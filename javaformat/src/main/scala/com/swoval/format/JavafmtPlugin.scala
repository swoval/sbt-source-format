package com.swoval.format

import com.swoval.format.java.JavaFormatter
import com.swoval.format.lib.SourceFormat
import sbt._

object JavafmtPlugin extends AutoPlugin with JavafmtKeys {
  override def trigger = allRequirements
  object autoImport extends JavafmtKeys

  override lazy val projectSettings = SourceFormat.settings(
    autoImport.javafmt,
    JavaFormatter,
    SourceFormat.compileSources(Compile, "*.java"),
    SourceFormat.compileSources(Test, "*.java"),
  )
}

private[format] trait JavafmtKeys {
  val javafmt = taskKey[Unit]("Format source files using the google java formatter.")
  val javafmtCheck =
    taskKey[Unit]("Check source file formatting using the google java formatter.")
}
