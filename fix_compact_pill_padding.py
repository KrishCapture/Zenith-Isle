with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

target = """@Composable
private fun CompactIslandPill(
    activeActivity: LiveActivityState,
    settings: IslandSettings
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),"""

replacement = """@Composable
private fun CompactIslandPill(
    activeActivity: LiveActivityState,
    settings: IslandSettings
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = if (activeActivity is LiveActivityState.Music) 4.dp else 12.dp,
                top = 8.dp,
                bottom = 8.dp
            ),"""

if target in text:
    text = text.replace(target, replacement)
else:
    print("Could not find target")

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
