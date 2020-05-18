sbt-source-format
===

| Travis CI |
|-----------|
|[ ![Linux build status][1]][2] |

[1]: https://travis-ci.org/swoval/sbt-source-format.svg?branch=master
[2]: https://travis-ci.org/swoval/sbt-source-format

A collection of sbt plugins for formatting source files. The
current version of the plugin is compatible with sbt 1.3.0 and greater. The
legacy version, 0.1.6, works with sbt 1.x and sbt 0.13.x.

There are four plugins in the family:

1. `sbt-clang-format` formats c family languages (c/c++/objc) using
clang-format.
2. `sbt-java-format` formats java source code using [google java formater](
    https://github.com/google/google-java-format).
3. `sbt-scala-format` formats scala source code using [scalafmt](
https://scalameta.org/scalafmt/).
4. `sbt-source-format` aggregates the first three plugins.
5. `sbt-jvm-format` aggregates (2) and (3) and provides keys for formatting all
of the java and scala sources (including sbt build files).

The latest version is `0.3.1`. To use the plugin, add one or more of
```
addSbtPlugin("com.swoval" % "sbt-clang-format" % "0.3.1")
addSbtPlugin("com.swoval" % "sbt-java-format" % "0.3.1")
addSbtPlugin("com.swoval" % "sbt-jvm-format" % "0.3.1")
addSbtPlugin("com.swoval" % "sbt-scala-format" % "0.3.1")
addSbtPlugin("com.swoval" % "sbt-source-format" % "0.3.1")
```
to your `project/plugins.sbt` file.

Usage
==
Each format plugin provides two tasks: one for formatting a source file
and overwriting its contents if they differ and one for checking that the
formatting is correct without overwriting. In the case of `sbt-clang-format`,
the format task is `clangfmt` and the check task is `clangfmtCheck`. Similarly,
the scala and java plugins provide `scalafmt`, `scalafmtCheck`, `javafmt` and
`javafmtCheck`. When using `sbt-jvm-format`, in addition to `javafmt`,
`scalafmt`, etc, there is also `jvmfmt`, which formats all of the java and scala
sources in the project for a configuration. The `jvmfmtAll` task formats all of
the java and scala sources in the `Compile` and `Test` tasks as well as the
scala and java sources that comprise the project sbt build.

Formatting is incremental. The plugin will only attempt to format files that
have not been previously formatted or verified as formatted. It also fully
interoperates with the `~` command. For example, running `~javafmt`, sbt will
watch for all of the "*.java" files and reformat whenever any are modified.

Source files
==
By default the `javafmt` source files will be all of the files ending with
`*.java` in the `unmanagedSourceDirectories`. The `javafmt` task is defined both
in the `Compile` and `Test` configurations as well as in the project
configuration.  Running `javafmt` will format all of the java source files in
the project while running `Test / javafmt` or `Compile / javafmt` will only
format the sources in the respective configurations.

For `clangfmt` no default sources are specified. Add `*.c` sources with:
```
clangfmt / fileInputs += baseDirectory.value.toGlob / "src" / ** / "*.c"
```
Sources can also be added to javafmt and scalafmt using `fileInputs` in
the appropriate configs, e.g.
```
Compile / scalafmt / fileInputs += baseDirectory.value.toGlob / "other" / ** / "*.scala"
```

Custom formatters
==
The plugin also provides a library for defining a custom formatter for any
source file type. To create a custom formatter, add
```
libraryDependencies += "com.swoval" %% "sbt-source-format-lib" % "0.2.3"
``` 
to `project/plugins.sbt` (or `build.sbt` for a plugin).

A no-op javascript formatter can be added to a build.sbt file like so:
```scala
import com.swoval.format.lib.SourceFormat
import java.nio.file.Files

val jsfmt = taskKey[Unit]("Format js files.")
SourceFormat.settings(
  jsfmt,
  (path, logger) => new String(Files.readAllBytes(path)),
  Def.setting(sourceDirectory.value.toGlob / ** / "*.js" :: Nil)
)
```

Troubleshooting
==

In order to use the clangfmt command, the clang-format utility must be
installed. On osx, it can be installed using homebrew: `brew install
clang-format`. On linux, it can be installed with apt: `apt-get install
clang-format` (sudo is probably necessary on most setups).

