package com.swoval.format

import java.io.File

/**
 * An exception thrown when a source file is not correctly formatted according to a source
 * formatter.
 */
trait UnformattedFileException extends Exception

/**
 * Provides implementations of [[UnformattedFileException]] for clang-format and javafmt.
 */
object UnformattedFileException {

  /**
   * A file is incorrectly formatted according to clang-format.
   * @param file the incorrectly formatted file.
   */
  case class Clang(file: File) extends UnformattedFileException

  /**
   * A file is incorrectly formatted according to javafmt.
   * @param file the incorrectly formatted file.
   */
  case class Java(file: File) extends UnformattedFileException
}
