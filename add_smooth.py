with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "r") as f:
    text = f.read()

# find IslandSettings
import re
text = re.sub(
    r"val enableDynamicTheme: Boolean = true,",
    "val enableDynamicTheme: Boolean = true,\n    val enableHyperSmoothMode: Boolean = false,",
    text
)

text = re.sub(
    r"fun toggleDynamicTheme\(enabled: Boolean\) \{\n        GlobalIslandState.settings.update \{ it.copy\(enableDynamicTheme = enabled\) \}\n    \}",
    "fun toggleDynamicTheme(enabled: Boolean) {\n        GlobalIslandState.settings.update { it.copy(enableDynamicTheme = enabled) }\n    }\n\n    fun toggleHyperSmoothMode(enabled: Boolean) {\n        GlobalIslandState.settings.update { it.copy(enableHyperSmoothMode = enabled) }\n    }",
    text
)

with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "w") as f:
    f.write(text)
