Global / onChangedBuildSource := ReloadOnSourceChanges

val scala212 = "2.12.10"

def baseVersion: String = "0.1.7-SNAPSHOT"

def commonSettings: SettingsDefinition =
  Def.settings(
    scalaVersion in ThisBuild := scala212,
    organization := "com.swoval",
    homepage := Some(url("https://github.com/swoval/sbt-source-format")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/swoval/sbt-source-format"),
        "git@github.com:swoval/sbt-source-format.git"
      )
    ),
    developers := List(
      Developer(
        "username",
        "Ethan Atkins",
        "contact@ethanatkins.com",
        url("https://github.com/eatkins")
      )
    ),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq("-feature"),
    publishTo := {
      val p = publishTo.value
      if (sys.props.get("SonatypeSnapshot").fold(false)(_ == "true"))
        Some(Opts.resolver.sonatypeSnapshots): Option[Resolver]
      else if (sys.props.get("SonatypeRelease").fold(false)(_ == "true"))
        Some(Opts.resolver.sonatypeReleases): Option[Resolver]
      else p
    },
    version in ThisBuild := {
      val v = baseVersion
      if (sys.props.get("SonatypeSnapshot").fold(false)(_ == "true")) {
        if (v.endsWith("-SNAPSHOT")) v else s"$v-SNAPSHOT"
      } else {
        v
      }
    },
  )

val `source-format` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    commonSettings,
    scriptedBufferLog := false,
    libraryDependencies += "com.google.googlejavaformat" % "google-java-format" % "1.6",
    libraryDependencies += "org.scalameta" %% "scalafmt-dynamic" % "2.1.0-RC2",
    dependencyOverrides := "org.scala-sbt" % "sbt" % "1.3.0" :: Nil,
    sbtVersion in pluginCrossBuild := "1.3.0",
    skip in publish :=
      !version.value
        .endsWith("-SNAPSHOT") || !sys.props.get("SonatypeSnapshot").fold(true)(_ == "true"),
    crossSbtVersions := Seq("1.3.0"),
    crossScalaVersions := Seq(scala212),
    name := "sbt-source-format",
    description := "Format source files using clang-format and the google java format library.",
  )
