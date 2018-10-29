package com.swoval.format

import com.swoval.format.SourceFormatPlugin.Source
import sbt._

/**
 * Finds all of the files in a given directory that are accepted by a FileFilter.
 */
object SourceExtractor extends (Source => Seq[File]) {

  /**
   * Finds all of the files in a given directory that are accepted by a FileFilter.
   *
   * @param source the tuple specifying the base directory, the FileFilter to apply to the
   *               source files in the base directory and a boolean flag toggling whether or not
   *               to recursively traverse the directory.
   * @return the filtered files in the directory.
   */
  def apply(source: Source): Seq[File] = {
    val (file, filter, recursive) = source
    val finder = PathFinder(file)
    if (recursive) finder.descendantsExcept(filter, NothingFilter).get else file.listFiles(filter)
  }
}
