package com.swoval.format

import com.swoval.format.clang.ClangFormatter
import com.swoval.format.lib.SourceFormat
import sbt.Keys.baseDirectory
import sbt._

object ClangfmtPlugin extends AutoPlugin {
  override def trigger = allRequirements
  trait Keys {
    val clangfmt = taskKey[Unit]("Format source files using clang format.")
    val clangfmtCheck = taskKey[Unit]("Check source file formatting using clang format.")
  }
  object autoImport extends Keys
  import autoImport._
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    SourceFormat.settings(
      clangfmt,
      ClangFormatter,
      Def.setting(Nil: Seq[Glob]),
      Def.setting(baseDirectory.value.toPath / ".clang-format")
    )
  )
}
