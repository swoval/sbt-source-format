package com.swoval.format

import java.io.File
import sbt.FileFilter

/**
 * A filter that accepts files if the file's extension is in the list of arguments. Usage:
 * {{{
 *   import com.swoval.format.ExtensionFilter
 *   import java.io.File
 *   val mixedSourceFilter = ExtensionFilter("java", "scala")
 *   println(mixedSourceFilter.accept(new File("foo.scala")) // prints true
 *   println(mixedSourceFilter.accept(new File("foo.java")) // prints true
 *   println(mixedSourceFilter.accept(new File("foo.cc")) // prints false
 * }}}
 * @param extensions the file extensions to accept
 */
case class ExtensionFilter(extensions: String*) extends FileFilter {
  private val set = extensions.toSet
  override def accept(pathname: File): Boolean = {
    val name = pathname.toString
    name.lastIndexOf('.') match {
      case -1                       => false
      case i if i < name.length - 1 => set(name.substring(i + 1))
      case _                        => set("")
    }
  }
}
