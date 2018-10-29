package com.swoval.format

import com.swoval.format.SourceFormatPlugin.Source
import sbt.{ Def, File, InputTask, SettingKey }

private[format] object Formatter {
  def apply(key: SettingKey[Seq[Source]],
            format: (File, Boolean) => Boolean,
            ex: File => UnformattedFileException): Def.Initialize[InputTask[Unit]] =
    Def.inputTask {
      val check = Def.spaceDelimited("<arg>").parsed.contains("--check")
      val sources = key.value.flatMap(SourceExtractor)
      sources.foreach(s => if (!format(s, check)) throw ex(s))
    }
}
