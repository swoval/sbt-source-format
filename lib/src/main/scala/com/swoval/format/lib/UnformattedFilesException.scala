package com.swoval.format.lib

import java.nio.file.Path

/**
 * An exception thrown when a source path is not correctly formatted according to a source
 * formatter.
 */
final class UnformattedFilesException(formatter: String, paths: Seq[Path])
    extends java.lang.Throwable(null, null, true, false) {
  override def getMessage: String = {
    val unformatted = if (paths.length == 1) paths.head.toString else paths.mkString("\n", "\n", "")
    s"Unformatted files according to $formatter: $unformatted"
  }
}
