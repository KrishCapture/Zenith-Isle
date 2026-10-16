with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re
text = re.sub(
    r"val rotationState = infiniteTransition\.animateFloat\([\s\S]*?label = \"vinyl_rotation_angle\"\n    \)",
    """val rotationState = if (settings.enableHyperSmoothMode) null else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation_angle"
    )
    val rotationAngle = if (settings.enableHyperSmoothMode || rotationState == null) 0f else rotationState.value""",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
