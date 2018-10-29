package com.swoval.format
package impl

import java.nio.file.Files
import java.io.File

import scala.sys.process._
import scala.util.Try

/**
 * Format a source file or verify that a file is correctly formatted using clang-format.
 */
private[format] object ClangFormatter extends ((File, Boolean) => Boolean) {

  /**
   * Format the file using [[https://clang.llvm.org/docs/ClangFormat.html clang-format]].
   * @param file the file to format
   * @param check only verify that the file is correctly formatted when true
   * @return true if the file is correctly formatted.
   */
  def apply(file: File, check: Boolean): Boolean = {
    val formatted = Seq("clang-format", file.toString).!!
    val original = new String(Files.readAllBytes(file.toPath))
    if (check) {
      formatted == original
    } else {
      formatted == original || Try(Files.write(file.toPath, formatted.getBytes)).isSuccess
    }
  }
}
