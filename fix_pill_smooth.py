with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Make waveform bar count smaller if hyper smooth
text = text.replace(
    "barCount = 28,",
    "barCount = if (settings.enableHyperSmoothMode) 10 else 28,"
)

# Disable glow if hyper smooth
text = text.replace(
    "val glowBrush = if (settings.enableGlow && isExpanded) {",
    "val glowBrush = if (settings.enableGlow && isExpanded && !settings.enableHyperSmoothMode) {"
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
