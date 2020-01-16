package com.swoval.format

import sbt.{ AutoPlugin, Def }

object SwovalSourceFormatGlobalPlugin extends AutoPlugin with lib.Keys {
  override def trigger = allRequirements
  object autoImport extends lib.Keys
  override val globalSettings: Seq[Def.Setting[_]] = Def.settings(
    formatTaskLimit := math.max(1, Runtime.getRuntime.availableProcessors / 2)
  )
}
