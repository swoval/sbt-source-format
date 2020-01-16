package com.swoval.format

import sbt._

object JvmPlugin extends AutoPlugin with JvmfmtKeys with JavafmtKeys with ScalafmtKeys {
  override def trigger = allRequirements
  object autoImport extends JvmfmtKeys

  override lazy val globalSettings: Seq[Def.Setting[_]] = Def.settings(
    jvmfmtOnCompile := false,
    javafmtOnCompile := (ThisBuild / jvmfmtOnCompile).value,
    scalafmtOnCompile := (ThisBuild / jvmfmtOnCompile).value,
  )
  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    Compile / jvmfmt := Seq(javafmt, scalafmt).map(Compile / _).join.value,
    Test / jvmfmt := Seq(javafmt, scalafmt).map(Test / _).join.value,
    Compile / jvmfmtCheck := Seq(javafmtCheck, scalafmtCheck).map(Compile / _).join.value,
    Test / jvmfmtCheck := Seq(javafmtCheck, scalafmtCheck).map(Test / _).join.value,
    jvmfmtAll := Seq(javafmtAll, scalafmtAll).join.value,
    jvmfmtCheckAll := Seq(javafmtCheckAll, scalafmtCheckAll).join.value,
  )
}

private[format] trait JvmfmtKeys {
  final val jvmfmt = taskKey[Unit]("Format source files using the google java formatter.")
  final val jvmfmtAll =
    taskKey[Unit]("Format all project source files using the google java formatter.")
  final val jvmfmtCheck =
    taskKey[Unit]("Check source file formatting using the google java formatter.")
  final val jvmfmtCheckAll =
    taskKey[Unit]("Check all project source file formatting using the google java formatter.")
  final val jvmfmtOnCompile =
    settingKey[Boolean]("Toggles whether to perform formatting before compilation.")
}
