scalafmtCoursierCachePath := Some(baseDirectory.value.toPath / "coursier")
InputKey[Unit]("checkFile") := {
  val file = Def.spaceDelimited("").parsed.head
  assert(fileTreeView.value.list(scalafmtCoursierCachePath.value.get.toGlob / **).exists {
    case (p, _) => p.getFileName.toString == file
  })
}
