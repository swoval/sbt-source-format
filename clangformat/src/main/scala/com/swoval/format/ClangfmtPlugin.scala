package com.swoval.format

import java.nio.file.Path

import com.swoval.format.clang.ClangFormatter
import com.swoval.format.lib.SourceFormat
import sbt.Keys.{ concurrentRestrictions, baseDirectory }
import sbt._

object ClangfmtPlugin extends AutoPlugin with ClangfmtKeys with lib.Keys {
  override def trigger = allRequirements
  object autoImport extends ClangfmtKeys
  private val clangFormatter =
    TaskKey[(Path, Path, Logger) => String]("clang-formatter", "", Int.MaxValue)
  override lazy val globalSettings: Seq[Def.Setting[_]] = Def.settings(
    Global / concurrentRestrictions += Tags
      .limit(ConcurrentRestrictions.Tag(clangfmt.key.label), (clangfmt / formatTaskLimit).value),
  )
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    clangFormatter := ClangFormatter,
    SourceFormat.settings(
      clangfmt,
      clangFormatter,
      Def.setting(Nil: Seq[Glob]),
      Def.setting(baseDirectory.value.toPath / ".clang-format")
    ),
  )
}

private[format] trait ClangfmtKeys {
  val clangfmt = taskKey[Unit]("Format source files using clang format.")
  val clangfmtCheck = taskKey[Unit]("Check source file formatting using clang format.")
}
