with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# Revert all of them first
text = re.sub(
    r"\.padding\(\s*start = 10\.dp,\s*end = if \(activeActivity is LiveActivityState\.Music\) 4\.dp else 10\.dp,\s*top = 6\.dp,\s*bottom = 6\.dp\s*\)",
    ".padding(horizontal = 12.dp, vertical = 8.dp)",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
