import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Remove the inline row
text = re.sub(
    r"Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(horizontal = 4\.dp\),\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\)\s*\{\s*val currentMins = \(songTimeMs / 1000\) / 60\s*val currentSecs = \(songTimeMs / 1000\) % 60\s*val remainingMs = \(totalTimeMs - songTimeMs\)\.coerceAtLeast\(0L\)\s*val remMins = \(remainingMs / 1000\) / 60\s*val remSecs = \(remainingMs / 1000\) % 60\s*Text\(\s*text = String\.format\(\"%02d:%02d\", currentMins, currentSecs\),\s*color = TextSecondary,\s*fontSize = 11\.sp,\s*fontWeight = FontWeight\.Medium\s*\)\s*Text\(\s*text = String\.format\(\"-%02d:%02d\", remMins, remSecs\),\s*color = TextMuted,\s*fontSize = 11\.sp\s*\)\s*\}",
    "DecoupledPlaybackTimeDisplay(songTimeMs = songTimeMs, totalTimeMs = totalTimeMs)",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
