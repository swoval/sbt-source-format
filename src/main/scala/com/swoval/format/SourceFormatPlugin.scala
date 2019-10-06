package com.swoval.format

import sbt._

/**
 * An sbt plugin that provides compileSources formatting tasks. The default tasks can either format the
 * task in place, or verify the source path formatting by adding the --check flag to the task key.
 */
object SourceFormatPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = NoTrigger
}
