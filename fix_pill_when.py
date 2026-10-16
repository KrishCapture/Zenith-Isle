import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

text = text.replace(
"""    val primaryColor = remember(activeActivity) {
        when (activeActivity) {
            is LiveActivityState.Music -> activeActivity.song.primaryColor""",
"""    val primaryColor = remember(activeActivity) {
        when (activeActivity) {
            is LiveActivityState.Idle -> Color.Transparent
            is LiveActivityState.Music -> activeActivity.song.primaryColor"""
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
