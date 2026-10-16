with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# Fix RealtimeWaveformView
text = re.sub(
    r"val infiniteTransition = rememberInfiniteTransition\(label = \"realtime_waveform\"\)\s*val phaseState = infiniteTransition\.animateFloat\([\s\S]*?label = \"waveform_phase\"\n    \)\s*val phase = if \(isSmoothMode\) 0f else phaseState\.value",
    """val phaseState = if (isSmoothMode) null else rememberInfiniteTransition(label = "realtime_waveform").animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )
    val phase = if (isSmoothMode || phaseState == null) 0f else phaseState.value""",
    text
)

# Fix vinyl rotation
text = re.sub(
    r"val infiniteTransition = rememberInfiniteTransition\(label = \"vinyl_rotate\"\)\s*val rotationState = infiniteTransition\.animateFloat\([\s\S]*?repeatMode = RepeatMode\.Restart\n    \)\s*val rotationAngle = if \(settings\.enableHyperSmoothMode\) 0f else rotationState\.value",
    """val rotationState = if (settings.enableHyperSmoothMode) null else rememberInfiniteTransition(label = "vinyl_rotate").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )
    val rotationAngle = if (settings.enableHyperSmoothMode || rotationState == null) 0f else rotationState.value""",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
