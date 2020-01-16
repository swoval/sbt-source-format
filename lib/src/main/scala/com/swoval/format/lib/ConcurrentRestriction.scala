package com.swoval.format
package lib

import sbt.{ ConcurrentRestrictions, Def, SettingKey, Tags }
import sbt.Keys.concurrentRestrictions

private[format] object ConcurrentRestriction {
  val tag = ConcurrentRestrictions.Tag("source-format")
  val limit = Tags.limit(tag, 1)
  val addLimit: Def.Setting[_] = sbt.Global / concurrentRestrictions ++= {
    val prev = (sbt.Global / concurrentRestrictions).value
    if (prev.contains(limit)) Nil else limit :: Nil
  }
  val parallelize = SettingKey[Boolean]("sourceFormatParallelize", "", Int.MaxValue)
}
