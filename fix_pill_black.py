with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# Fix main pill background to just be IslandBlack when open, or maybe use bgOpacity only when compact
text = re.sub(
    r"\.background\(\s*if\s*\(isExpanded\s*&&\s*activeActivity\s*is\s*LiveActivityState\.Music\).*?androidx\.compose\.ui\.graphics\.SolidColor\(islandBgColor\)\s*\)",
    ".background(islandBgColor)",
    text,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
