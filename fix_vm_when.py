with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "r") as f:
    text = f.read()

text = text.replace(
"""        val activeRunning = when (active) {
            is LiveActivityState.Music -> active.isPlaying""",
"""        val activeRunning = when (active) {
            is LiveActivityState.Idle -> false
            is LiveActivityState.Music -> active.isPlaying"""
)

with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "w") as f:
    f.write(text)
