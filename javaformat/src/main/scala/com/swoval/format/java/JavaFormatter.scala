package com.swoval.format.java

import java.net.URLClassLoader
import java.nio.file.{ Files, Path }

import com.google.googlejavaformat.java.Formatter
import sbt.util.Logger

/**
 * Formats a source path or verifies that the path is correctly formatted using
 * [[https://github.com/google/google-java-format google java formatter]].
 */
object JavaFormatter extends ((Path, Path, Logger) => String) {
  private val javaFormatter: (Path, Path, Logger) => String = {
    val loader = this.getClass.getClassLoader match {
      case l: URLClassLoader =>
        val sorted = l.getURLs.toSeq.sortBy(u => if (u.toString.contains("guava")) -1 else 1)
        new URLClassLoader(sorted.toArray, l.getParent)
      case l => l
    }
    loader
      .loadClass("com.swoval.format.java.JavaFormatterImpl$")
      .getDeclaredField("MODULE$")
      .get(null)
  }.asInstanceOf[(Path, Path, Logger) => String]

  /**
   * Format the path using [[https://github.com/google/google-java-format google java formatter]].
   * @param path the path to format
   * @return true if the path is correctly formatted.
   */
  def apply(unused: Path, path: Path, logger: Logger): String = javaFormatter(unused, path, logger)
  override def toString = "JavaFormatter"
}

private[format] object JavaFormatterImpl extends ((Path, Path, Logger) => String) {
  private val formatter = new Formatter()
  def apply(unused: Path, path: Path, logger: Logger): String =
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
}
