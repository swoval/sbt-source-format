import com.swoval.format.lib.SourceFormat
import java.nio.file.Files

val jsfmt = taskKey[Unit]("Format js files.")
SourceFormat.settings(
  jsfmt,
  (path, _) => new String(Files.readAllBytes(path)) + "\n",
  Def.setting(sourceDirectory.value.toGlob / ** / "*.js" :: Nil)
)

TaskKey[Unit]("check") := {
  jsfmt.value
  val file = sourceDirectory.value.toPath / "js" / "foo.js"
  assert(new String(Files.readAllBytes(file)) == "function foo(x) { return x }\n")
}
