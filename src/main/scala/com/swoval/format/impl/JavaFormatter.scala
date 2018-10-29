package com.swoval.format
package impl

import java.nio.file.Files

import com.google.googlejavaformat.java.Formatter
import sbt._

import scala.util.Try

/**
 * Formats a source file or verifies that the file is correctly formatted using
 * [[https://github.com/google/google-java-format google java formatter]].
 */
private[format] object JavaFormatter extends ((File, Boolean) => Boolean) {
  private val formatter = new Formatter()

  /**
   * Format the file using [[https://github.com/google/google-java-format google java formatter]].
   * @param file the file to format
   * @param check only verify that the file is correctly formatted when true
   * @return true if the file is correctly formatted.
   */
  def apply(file: File, check: Boolean): Boolean = {
    val original = new String(Files.readAllBytes(file.toPath))
    val formatted = formatter.formatSource(original)
    if (check) {
      original == formatted
    } else {
      original == formatted || Try(Files.write(file.toPath, formatted.getBytes)).isSuccess
    }
  }
}
