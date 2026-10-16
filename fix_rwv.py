with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    pill_text = f.read()

pill_text = pill_text.replace(
    "barCount = 6,\n                            modifier = Modifier.fillMaxSize()\n                        )",
    "barCount = 6,\n                            isSmoothMode = settings.enableHyperSmoothMode,\n                            modifier = Modifier.fillMaxSize()\n                        )"
)

pill_text = pill_text.replace(
    "barCount = 10,\n                            modifier = Modifier.fillMaxSize()\n                        )",
    "barCount = 10,\n                            isSmoothMode = settings.enableHyperSmoothMode,\n                            modifier = Modifier.fillMaxSize()\n                        )"
)

pill_text = pill_text.replace(
    "barCount = if (settings.enableHyperSmoothMode) 10 else 28,\n                    modifier = Modifier.fillMaxSize()\n                )",
    "barCount = if (settings.enableHyperSmoothMode) 10 else 28,\n                    isSmoothMode = settings.enableHyperSmoothMode,\n                    modifier = Modifier.fillMaxSize()\n                )"
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(pill_text)
