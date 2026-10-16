with open("app/src/main/java/com/example/ui/components/SettingsDrawer.kt", "r") as f:
    text = f.read()

import re

hyper_smooth_ui = """                SettingsToggleRow(
                    icon = Icons.Default.Speed,
                    title = "HyperOS Smooth Mode",
                    subtitle = "Optimizes animations and graphics for maximum performance",
                    checked = settings.enableHyperSmoothMode,
                    onCheckedChange = { viewModel.toggleHyperSmoothMode(it) }
                )
"""
text = re.sub(
    r"(import androidx.compose.material.icons.filled.Settings)",
    r"\1\nimport androidx.compose.material.icons.filled.Speed",
    text
)

text = re.sub(
    r"(SettingsToggleRow\([\s\S]*?title = \"Enable Glow\"[\s\S]*?\))",
    r"\1\n\n" + hyper_smooth_ui,
    text
)

with open("app/src/main/java/com/example/ui/components/SettingsDrawer.kt", "w") as f:
    f.write(text)
