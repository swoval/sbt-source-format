import com.swoval.format.ExtensionFilter

val root = (project in file(".")).settings(
  clangfmtSources += (baseDirectory.value / "src" / "main" / "native", ExtensionFilter("cc"), true)
)
