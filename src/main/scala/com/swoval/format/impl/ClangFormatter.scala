package com.swoval.format
package impl

import java.nio.file.Path

import scala.collection.JavaConverters._

/**
 * Format a source path or verify that a path is correctly formatted using clang-format.
 */
private[format] object ClangFormatter extends ((Path, Path) => String) {

  /**
   * Format the path using [[https://clang.llvm.org/docs/ClangFormat.html clang-format]].
   * @param path the path to format
   * @return true if the path is correctly formatted.
   */
  def apply(config: Path, path: Path): String = {
    val formatCmd = System.getProperty("swoval.format.cmd", "clang-format")
    val proc =
      new ProcessBuilder(formatCmd, path.toString).directory(config.getParent.toFile).start()
    val result = new java.util.Vector[Byte]
    val error = new java.util.Vector[Byte]
    def drain() {
      while (proc.getInputStream.available > 0) {
        result.add((proc.getInputStream.read & 0xFF).toByte)
      }
      while (proc.getErrorStream.available > 0) {
        error.add((proc.getErrorStream.read & 0xFF).toByte)
      }
    }
    val thread = new Thread() {
      setDaemon(true)
      start()
      override def run(): Unit = {
        while (proc.isAlive) {
          drain()
          Thread.sleep(2)
        }
      }
    }
    proc.waitFor() match {
      case 0 =>
        thread.join(5000)
        drain()
        new String(result.asScala.toArray)
      case i =>
        val msg = s"clang-format exited with error code $i\n:" + new String(error.asScala.toArray)
        throw new IllegalStateException(msg)
    }
  }
  override def toString = "ClangFormatter"
}
