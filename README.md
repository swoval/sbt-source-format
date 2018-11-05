sbt-source-format
===

| Travis CI |
|-----------|
|[ ![Linux build status][1]][2] |

[1]: https://travis-ci.org/swoval/sbt-source-format.svg?branch=master
[2]: https://travis-ci.org/swoval/sbt-source-format

A simple sbt plugin for formatting java and c family sources (c/c++/objc). The plugin is compatible
with sbt 1.0 and sbt 0.13. Api documentation is available at
[sbt-source-format](https://swoval.github.io/docs/sbt-source-format/0.1.4/api/com/swoval/format).

The latest version is `0.1.4`. To use the plugin, add
```
addSbtPlugin("com.swoval" % "sbt-source-format" % "0.1.4")
```
to your `project/plugins.sbt` file, or it add it globally to `~/.sbt/1.0/plugins/global.sbt` or
 `~/.sbt/0.13/plugins/global.sbt` to use it in all projects.

In order to use the clangfmt command, the clang-format utility must be installed. On osx, it can
be installed using homebrew: `brew install clang-format`. On linux, it can be installed with apt:
`apt-get install clang-format` (sudo is probably necessary on most setups).

Usage
==
To format java sources using
[google java formater](https://github.com/google/google-java-format), run
```
javafmt
```
To format native sources using [clang-format](https://clang.llvm.org/docs/ClangFormat.html), run
```
clangfmt
```
Note that both of these commands will have the side effect of overwriting each of the original files
that are not formatted correctly with a formatted version. To verify that all sources are formatted
correctly without actually modifying any of the original source, run the commands with the `--check`
flag, e.g.
```
javafmt --check
```

Source files
==
The files to format can be configured with the `clangfmtSources` and `javafmtSources` keys. By
default, the clangfmt sources will include all of the files in the `unmanagedSourceDirectories`
having an extension in the set { c, cc, cpp, cxx, h, hh, hpp, hxx }. The default javafmt sources
will be all of the files ending with `*.java` in the `unmanagedSourceDirectories`. Source
directories may be added by specifying a `(File, FileFilter, Boolean)` where the `File` argument
is the base source directory, the `FileFilter` controls which source files in that directory are
accepted and the `Boolean` parameter toggles whether the directory is recursive. Add a custom
source directory with
```scala
import com.swoval.format.ExtensionFilter
clangfmtSources += (baseDirectory.value / "src" / "main" / "native", ExtensionFilter("c", "h"), true)
```
