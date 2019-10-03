package com.swoval.format
package impl

import java.nio.file.Path

import scala.sys.process._

/**
 * Format a source path or verify that a path is correctly formatted using clang-format.
 */
private[format] object ClangFormatter extends (Path => String) {

  /**
   * Format the path using [[https://clang.llvm.org/docs/ClangFormat.html clang-format]].
   * @param path the path to format
   * @return true if the path is correctly formatted.
   */
  def apply(path: Path): String = {
    val formatCmd = System.getProperty("swoval.format.cmd", "clang-format")
    Seq(formatCmd, path.toString).!!
  }
  override def toString = "ClangFormatter"
}
