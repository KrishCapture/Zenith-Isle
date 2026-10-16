with open("app/src/main/java/com/example/ui/components/PhoneSimulatorCanvas.kt", "r") as f:
    text = f.read()

import re

new_text = re.sub(
    r"val wallpaperGradient = Brush.radialGradient\([\s\S]*?radius = 1200f\n    \)",
    """val wallpaperGradient = remember {
        Brush.radialGradient(
            colors = listOf(
                PrimaryNeon.copy(alpha = 0.22f),
                SecondaryNeon.copy(alpha = 0.12f),
                DarkBackground
            ),
            radius = 1200f
        )
    }""",
    text
)

with open("app/src/main/java/com/example/ui/components/PhoneSimulatorCanvas.kt", "w") as f:
    f.write(new_text)
