package sbt
import java.nio.file.{ Path => JPath }

import sbt.nio.{ FileStamp, FileStamper }

object SourceFormatWrappers {
  val unmanagedFileStampCache = sbt.nio.Keys.unmanagedFileStampCache
  object NoCache extends sbt.nio.FileStamp.Cache {
    override def invalidate(path: JPath): Unit = {}
    override def get(path: JPath): Option[FileStamp] = None
    override def getOrElseUpdate(path: JPath, stamper: FileStamper): Option[FileStamp] =
      FileStamp(path, stamper)

    override def remove(key: JPath): Option[FileStamp] = None
    override def put(key: JPath, fileStamp: FileStamp): Option[FileStamp] = None
    override def putIfAbsent(
        key: JPath,
        stamper: FileStamper
    ): (Option[FileStamp], Option[FileStamp]) = (None, FileStamp(key, stamper))

    override def update(key: JPath, stamper: FileStamper): (Option[FileStamp], Option[FileStamp]) =
      (None, FileStamp(key, stamper))
  }
}
