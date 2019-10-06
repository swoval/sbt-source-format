package com.swoval.format

import com.swoval.format.java.JavaFormatter
import com.swoval.format.lib.SourceFormat
import sbt._

object JavafmtPlugin extends AutoPlugin {
  override def trigger = allRequirements
  trait Keys {
    val javafmt = taskKey[Unit]("Format source files using the google java formatter.")
    val javafmtCheck =
      taskKey[Unit]("Check source file formatting using the google java formatter.")
  }
  object autoImport extends Keys

  import autoImport._
  override lazy val projectSettings = SourceFormat.settings(
    javafmt,
    JavaFormatter,
    SourceFormat.compileSources(Compile, "*.java"),
    SourceFormat.compileSources(Test, "*.java"),
  )
}
