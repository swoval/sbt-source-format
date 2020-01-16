import sbt.nio.FileStamper

val check = inputKey[Unit]("")
check := {
  val changeCount = Def.spaceDelimited("").parsed.head.toInt
  val changes = checkChanges.value
  assert(
    changes.modified.size == changeCount,
    s"$changes had ${changes.modified.size} $changeCount modified files"
  )
}
val checkChanges = taskKey[FileChanges]("")
checkChanges := checkChanges.inputFileChanges
checkChanges / inputFileStamper := FileStamper.Hash
checkChanges / fileInputs := baseDirectory.value.toGlob / ** / "*.{sbt,java,scala}" :: Nil
