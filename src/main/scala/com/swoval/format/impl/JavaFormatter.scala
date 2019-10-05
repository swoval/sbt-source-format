package com.swoval.format
package impl

import java.nio.file.{ Files, Path }

import com.google.googlejavaformat.java.Formatter

/**
 * Formats a source path or verifies that the path is correctly formatted using
 * [[https://github.com/google/google-java-format google java formatter]].
 */
private[format] object JavaFormatter extends ((Path, Path) => String) {
  private val formatter = new Formatter()

  /**
   * Format the path using [[https://github.com/google/google-java-format google java formatter]].
   * @param path the path to format
   * @return true if the path is correctly formatted.
   */
  def apply(unused: Path, path: Path): String =
    try {
      val original = new String(Files.readAllBytes(path))
      formatter.formatSource(original)
    } catch {
      case e: NoSuchMethodError =>
        e.printStackTrace(System.err)
        throw e
      case e: Exception =>
        System.err.println(s"Couldn't format path: $path ($e)")
        throw e
    }
  override def toString = "JavaFormatter"
}
