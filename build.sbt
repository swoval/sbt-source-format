Global / onChangedBuildSource := ReloadOnSourceChanges

val scala212 = "2.12.10"

ThisBuild / version := "0.2.4-SNAPSHOT"

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
      else if (sys.props.get("SonatypeStaging").fold(false)(_ == "true"))
        Some(Opts.resolver.sonatypeStaging): Option[Resolver]
      else p
    }
  )

val lib = project.settings(
  commonSettings,
  libraryDependencies += "org.scala-sbt" % "sbt" % "1.3.0",
  crossScalaVersions := Seq(scala212),
  name := "sbt-source-format-lib",
)

def pluginSettings: Seq[Def.Setting[_]] = Def.settings(
  commonSettings,
  exportJars := true,
  scripted := scripted.dependsOn(lib / publishLocal).evaluated,
  dependencyOverrides := "org.scala-sbt" % "sbt" % "1.3.0" :: Nil,
  scriptedBufferLog := false,
  sbtVersion in pluginCrossBuild := "1.3.0",
  crossSbtVersions := Seq("1.3.0"),
  crossScalaVersions := Seq(scala212),
)
val clangformat = project
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    name := "sbt-clang-format",
    description := "Format source files using clang-format.",
  )
  .dependsOn(lib % "compile->compile")

val javaformat = project
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    libraryDependencies += "com.google.googlejavaformat" % "google-java-format" % "1.6",
    name := "sbt-java-format",
    description := "Format source files using javaformat.",
  )
  .dependsOn(lib % "compile->compile")

val scalaformat = project
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    libraryDependencies += "org.scalameta" %% "scalafmt-dynamic" % "2.1.0-RC2",
    name := "sbt-scala-format",
    description := "Format source files using scalafmt.",
  )
  .dependsOn(lib % "compile->compile")

// The root project aggregates the library and three plugins via depends on
val `sbt-source-format` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    scripted := scripted
      .dependsOn(clangformat / publishLocal, javaformat / publishLocal, scalaformat / publishLocal)
      .evaluated,
    aggregate in publish := false,
    name := "sbt-source-format",
    description := "Format source files using clang-format, scalafmt and the google java format library.",
  )
  .dependsOn(
    lib % "compile->compile",
    clangformat % "compile->compile",
    javaformat % "compile->compile",
    scalaformat % "compile->compile",
  )
  .aggregate(lib, clangformat, javaformat, scalaformat)

def release(local: Boolean): Def.Initialize[Task[Seq[Unit]]] = Def.taskDyn {
  val _ = (
    (`sbt-source-format` / Compile / scalafmtCheck).value,
    (`sbt-source-format` / Test / scalafmtCheck).value,
  )
  val versions =
    Seq(lib / version, clangformat / version, javaformat / version, scalaformat / version).join
  val publishKey = if (local) publishLocal else publish
  Def.taskDyn {
    val v = (ThisBuild / version).value
    val msg = s"Version $v was ${if (local) "not" else ""} a snapshot version"
    assert(v.endsWith("-SNAPSHOT") == local, msg)
    assert(versions.value.forall(_ == v))
    Seq(lib, clangformat, javaformat, scalaformat, `sbt-source-format`).map(_ / publishKey).join
  }
}
TaskKey[Unit]("release") := release(local = false).value

TaskKey[Unit]("releaseLocal") := release(local = true).value
