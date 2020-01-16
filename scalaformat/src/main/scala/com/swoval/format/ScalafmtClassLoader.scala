package com.swoval.format

import java.net.{ URL, URLClassLoader }
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

import com.swoval.format.lib.SourceFormat.FormatException
import com.swoval.format.scala.ScalafmtImpl
import coursier.cache.FileCache
import coursier.core.{ Module, ModuleName, Organization }
import coursier.util.Task
import coursier.{ Dependency, Fetch }
import sbt.util.Logger

private[format] object ScalafmtClassLoader {
  private[this] val formatters = new ConcurrentHashMap[String, (Path, Path, Logger) => String]()
  def apply(version: String, cacheDirectory: Option[Path]): (Path, Path, Logger) => String =
    formatters.get(version) match {
      case null =>
        val d = Dependency(
          Module(Organization("org.scalameta"), ModuleName("scalafmt-dynamic_2.12"), Map.empty),
          version
        )
        implicit val s: Task.sync.type = Task.sync
        val cache =
          cacheDirectory.map(cd => FileCache().withLocation(cd.toFile)).getOrElse(FileCache())
        val files = Fetch(cache).addDependencies(d).run()

        val resourceName = ScalafmtClassLoader.getClass.getCanonicalName
          .replace('.', '/') + ".class"
        val pluginURL = new URL(
          ScalafmtClassLoader.getClass.getClassLoader
            .getResource(resourceName)
            .toString
            .replace(resourceName, "")
        )
        val loader = new URLClassLoader(
          files.toArray.map(_.toURI.toURL) :+ pluginURL,
          Thread.currentThread.getContextClassLoader
        ) {
          private[this] object noScala extends ClassNotFoundException
          override def loadClass(name: String, resolve: Boolean): Class[_] = {
            try {
              if (name.startsWith("scala.")) throw noScala
              getClassLoadingLock(name).synchronized {
                val clazz = findClass(name)
                if (resolve) resolveClass(clazz)
                clazz
              }
            } catch { case _: ClassNotFoundException => super.loadClass(name, resolve) }
          }
        }
        val clazz = loader.loadClass(classOf[ScalafmtImpl].getCanonicalName);
        val formatter = clazz
          .getDeclaredMethod("get")
          .invoke(null)
          .asInstanceOf[(Path, Path, Consumer[String]) => String]
        val res: (Path, Path, Logger) => String = (c, p, l) =>
          try formatter(c, p, s => l.info(s))
          catch {
            case e if e.getClass.getCanonicalName.contains("IllegalStateException") =>
              throw new FormatException(e.getCause.getMessage)
          }
        formatters.put(version, res)
        res
      case f => f
    }
}
