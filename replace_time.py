import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

def get_block(text, start_str):
    start = text.find(start_str)
    if start == -1: return -1, -1
    open_braces = 0
    in_block = False
    for i in range(start, len(text)):
        if text[i] == '{':
            open_braces += 1
            in_block = True
        elif text[i] == '}':
            open_braces -= 1
        
        if in_block and open_braces == 0:
            return start, i + 1
    return -1, -1

start, end = get_block(text, "private fun DecoupledPlaybackTimeDisplay(")
if start != -1:
    new_func = """@Composable
private fun DecoupledPlaybackTimeDisplay(songTimeMs: Long, totalTimeMs: Long, primaryColor: Color = Color.White) {
    val currentMins = (songTimeMs / 1000) / 60
    val currentSecs = (songTimeMs / 1000) % 60
    val remainingMs = (totalTimeMs - songTimeMs).coerceAtLeast(0L)
    val remMins = (remainingMs / 1000) / 60
    val remSecs = (remainingMs / 1000) % 60
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d:%02d", currentMins, currentSecs),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        // Progress Slider
        androidx.compose.material3.Slider(
            value = songTimeMs.toFloat(),
            onValueChange = {}, // read-only in this decoupled view for now, or handle state hoisting
            valueRange = 0f..totalTimeMs.toFloat(),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.White.copy(alpha = 0.9f),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(16.dp)
                .testTag("song_time_slider")
        )

        Text(
            text = String.format("%02d:%02d", totalTimeMs / 60000, (totalTimeMs / 1000) % 60),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}
"""
    text = text[:start] + new_func + text[end:]
    
    # Also I might need to import offset
    if "import androidx.compose.foundation.layout.offset" not in text:
        text = text.replace("import androidx.compose.foundation.layout.padding", "import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.offset")
    
    # I might need to import Icons.Default.Headphones
    if "import androidx.compose.material.icons.filled.Headphones" not in text:
        text = text.replace("import androidx.compose.material.icons.filled.Favorite", "import androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.Headphones")
        
    with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
        f.write(text)
    print("Replaced DecoupledPlaybackTimeDisplay")
else:
    print("Could not find DecoupledPlaybackTimeDisplay")
