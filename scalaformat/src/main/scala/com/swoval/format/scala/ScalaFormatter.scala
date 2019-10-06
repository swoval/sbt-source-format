package com.swoval.format.scala

import java.io._
import java.nio.file.{ Files, Path }

import org.scalafmt.interfaces.{ Scalafmt, ScalafmtReporter }
import sbt.util.Logger
import scala.collection.JavaConverters._

object ScalaFormatter extends ((Path, Path, Logger) => String) {
  private def reporter(logger: Logger): ScalafmtReporter = new ScalafmtReporter {
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

  override def apply(config: Path, path: Path, logger: Logger): String =
    globalInstance
      .withReporter(reporter(logger))
      .format(config, path, new String(Files.readAllBytes(path)))
}
