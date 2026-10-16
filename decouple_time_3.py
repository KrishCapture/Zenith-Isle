with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re
text = re.sub(
    r"Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.padding\(horizontal = 4\.dp\),\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\)\s*\{\s*val currentMins = \(songTimeMs / 1000\) / 60[\s\S]*?text = String\.format\(\"-%02d:%02d\", remMins, remSecs\),\s*color = TextMuted,\s*fontSize = 11\.sp\s*\)\s*\}",
    "DecoupledPlaybackTimeDisplay(songTimeMs = songTimeMs, totalTimeMs = totalTimeMs)",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
