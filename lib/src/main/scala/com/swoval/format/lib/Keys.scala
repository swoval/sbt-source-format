package com.swoval.format.lib

import sbt.settingKey

private[format] trait Keys {
  final val formatTaskLimit =
    settingKey[Int]("The maximum number of sbt tasks to create for each format task.")
}
