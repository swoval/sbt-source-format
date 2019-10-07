package com.swoval.format

import com.swoval.format.clang.ClangFormatter
import com.swoval.format.lib.SourceFormat
import sbt.Keys.baseDirectory
import sbt._

object ClangfmtPlugin extends AutoPlugin {
  override def trigger = allRequirements
  object autoImport extends ClangfmtKeys
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    SourceFormat.settings(
      autoImport.clangfmt,
      ClangFormatter,
      Def.setting(Nil: Seq[Glob]),
      Def.setting(baseDirectory.value.toPath / ".clang-format")
    )
  )
}

private[format] trait ClangfmtKeys {
  val clangfmt = taskKey[Unit]("Format source files using clang format.")
  val clangfmtCheck = taskKey[Unit]("Check source file formatting using clang format.")
}
