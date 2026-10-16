with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# Remove bgOpacity from islandBgColor
text = re.sub(
    r"val islandBgColor = remember\(settings\.enableDynamicTheme, isDarkSystem, bgOpacity\) \{\s*if \(settings\.enableDynamicTheme && !isDarkSystem\) \{\s*Color\(0xFF0F1017\)\.copy\(alpha = bgOpacity\)\s*\} else \{\s*IslandBlack\.copy\(alpha = bgOpacity\)\s*\}\s*\}",
    """val islandBgColor = remember(settings.enableDynamicTheme, isDarkSystem) {
        if (settings.enableDynamicTheme && !isDarkSystem) {
            Color(0xFF0F1017)
        } else {
            IslandBlack
        }
    }""",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
