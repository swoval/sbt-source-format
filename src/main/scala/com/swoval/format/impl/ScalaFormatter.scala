package com.swoval.format.impl

import java.io.{ BufferedOutputStream, OutputStream, OutputStreamWriter, PrintStream, PrintWriter }
import java.nio.file.{ Files, Path }

import org.scalafmt.interfaces.{ Scalafmt, ScalafmtReporter }
import sbt.internal.util.{ ConsoleAppender, ConsoleLogger }
import scala.collection.JavaConverters._

object ScalaFormatter extends ((Path, Path) => String) {
  private def reporter: ScalafmtReporter = new ScalafmtReporter {
    override def error(file: Path, message: String): Unit = {
      throw new IllegalStateException(s"$file couldn't be formatted with scalafmt: $message")
    }
    override def error(file: Path, e: Throwable): Unit = {
      throw new IllegalStateException(s"$file couldn't be formatted with scalafmt", e)
    }
    override def excluded(file: Path): Unit = {}
    override def parsedConfig(config: Path, scalafmtVersion: String): Unit = {}
    override def downloadWriter(): PrintWriter = new PrintWriter(System.out)
    override def downloadOutputStreamWriter(): OutputStreamWriter = {
      val logger = ConsoleLogger()
      new OutputStreamWriter(new PrintStream(new BufferedOutputStream(new OutputStream {
        val buffer = new java.util.Vector[Byte]
        override def write(b: Int): Unit = {
          if (b == 10 || b == 13) {
            logger.info(new String(buffer.asScala.toArray))
            buffer.clear()
          } else {
            buffer.add((b & 0xFF).toByte)
          }
        }
      })))
    }
  }
  private val globalInstance = Scalafmt.create(this.getClass.getClassLoader)

  override def apply(config: Path, path: Path): String =
    globalInstance.withReporter(reporter).format(config, path, new String(Files.readAllBytes(path)))
}
