package com.swoval.format

import java.nio.file.Path

/**
 * An exception thrown when a source path is not correctly formatted according to a source
 * formatter.
 */
sealed abstract class UnformattedFilesException
    extends java.lang.Throwable(null, null, true, false) {
  def paths: Seq[Path]
  override def getMessage: String =
    if (paths.length == 1) paths.head.toString else paths.mkString("\n", "\n", "")
}

/**
 * Provides implementations of [[UnformattedFilesException]] for clang-format and javafmt.
 */
object UnformattedFilesException {

  /**
   * A path is incorrectly formatted according to clang-format.
   * @param paths the incorrectly formatted paths.
   */
  final case class Clang(paths: Path*) extends UnformattedFilesException

  /**
   * A path is incorrectly formatted according to javafmt.
   * @param paths the incorrectly formatted path.
   */
  final case class Java(paths: Path*) extends UnformattedFilesException

  /**
   * A path is incorrectly formatted according to scalafmt.
   * @param paths the incorrectly formatted path.
   */
  final case class Scala(paths: Path*) extends UnformattedFilesException
}
