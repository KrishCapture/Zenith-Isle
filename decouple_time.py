import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

decoupled_composable = """@Composable
private fun DecoupledPlaybackTimeDisplay(songTimeMs: Long, totalTimeMs: Long) {
    val currentMins = (songTimeMs / 1000) / 60
    val currentSecs = (songTimeMs / 1000) % 60
    val remainingMs = (totalTimeMs - songTimeMs).coerceAtLeast(0L)
    val remMins = (remainingMs / 1000) / 60
    val remSecs = (remainingMs / 1000) % 60
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = String.format("%02d:%02d", currentMins, currentSecs),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = String.format("-%02d:%02d", remMins, remSecs),
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}
"""

# Now replace the inline row with the decoupled composable
text = re.sub(
    r"Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(horizontal = 4\.dp\),\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\)\s*\{\s*val currentMins = \(songTimeMs / 1000\) / 60[\s\S]*?\}\s*\n\s*\}",
    "DecoupledPlaybackTimeDisplay(songTimeMs = songTimeMs, totalTimeMs = totalTimeMs)\n        }",
    text
)

# And add the composable at the bottom
text = text + "\n" + decoupled_composable

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
