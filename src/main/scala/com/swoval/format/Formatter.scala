package com.swoval.format

import com.swoval.format.SourceFormatPlugin.Source
import sbt.{ Def, File, InputTask, SettingKey }
import sbt.Keys.streams

private[format] object Formatter {
  def apply(
    name: SettingKey[String],
    key: SettingKey[Seq[Source]],
            format: (File, Boolean) => Boolean,
            ex: File => UnformattedFileException): Def.Initialize[InputTask[Unit]] =
    Def.inputTask {
      val logger = streams.value.log
      val check = Def.spaceDelimited("<arg>").parsed.contains("--check")
      val sources = key.value.flatMap(SourceExtractor)
      val len = sources.length
      logger.info(s"Formatting $len source${if (len > 1) "s" else ""} in ${name.value} using $format.")
      sources
        .collect {
          case s if !format(s, check) =>
            logger.error(s"$s is not correctly formatted according to $format.")
            ex(s)
        }
        .foreach(throw _)
    }
}
