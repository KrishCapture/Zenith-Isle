import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    pill_text = f.read()

# Update EqualizerBars call
pill_text = pill_text.replace(
    "maxHeight = 14.dp\n                    )",
    "maxHeight = 14.dp,\n                        isSmoothMode = settings.enableHyperSmoothMode\n                    )"
)

# Update RealtimeWaveform definition
pill_text = pill_text.replace(
    "barCount: Int = 20\n)",
    "barCount: Int = 20,\n    isSmoothMode: Boolean = false\n)"
)

# In RealtimeWaveform, don't use phase if smooth mode
pill_text = pill_text.replace(
    "val phase by infiniteTransition.animateFloat(",
    "val phaseState = infiniteTransition.animateFloat("
)
pill_text = pill_text.replace(
    "        label = \"waveform_phase\"\n    )",
    "        label = \"waveform_phase\"\n    )\n    val phase = if (isSmoothMode) 0f else phaseState.value"
)

# In vinyl rotation
pill_text = pill_text.replace(
    "val rotationAngle by infiniteTransition.animateFloat(",
    "val rotationState = infiniteTransition.animateFloat("
)
pill_text = pill_text.replace(
    "        repeatMode = RepeatMode.Restart\n    )",
    "        repeatMode = RepeatMode.Restart\n    )\n    val rotationAngle = if (settings.enableHyperSmoothMode) 0f else rotationState.value"
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(pill_text)
