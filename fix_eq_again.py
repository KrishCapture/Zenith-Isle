with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# CompactRightContent Eq: the wave in pill is on edge
target = """                    EqualizerBars(
                        isPlaying = activity.isPlaying,
                        color = activity.song.primaryColor,
                        barCount = 4,
                        maxHeight = 12.dp,"""

replacement = """                    EqualizerBars(
                        isPlaying = activity.isPlaying,
                        color = activity.song.primaryColor,
                        barCount = 4,
                        maxHeight = 10.dp,"""

if target in text:
    text = text.replace(target, replacement)
else:
    print("Could not find eq target")

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
