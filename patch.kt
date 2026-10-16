@Composable
private fun DecoupledPlaybackTimeDisplay(songTimeMs: Long, totalTimeMs: Long, primaryColor: Color = Color.White, isPlaying: Boolean = false) {
    // Local fast-updating UI state to prevent parent recomposition
    var displayTimeMs by androidx.compose.runtime.mutableStateOf(songTimeMs)

    androidx.compose.runtime.LaunchedEffect(isPlaying, songTimeMs) {
        displayTimeMs = songTimeMs
        if (isPlaying) {
            var lastTime = System.currentTimeMillis()
            while (true) {
                kotlinx.coroutines.delay(100)
                val now = System.currentTimeMillis()
                displayTimeMs = (displayTimeMs + (now - lastTime)).coerceAtMost(totalTimeMs)
                lastTime = now
            }
        }
    }

    val currentMins = (displayTimeMs / 1000) / 60
    val currentSecs = (displayTimeMs / 1000) % 60
    val remainingMs = (totalTimeMs - displayTimeMs).coerceAtLeast(0L)
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d:%02d", currentMins, currentSecs),
            color = primaryColor,
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
        
        // Progress Slider
        androidx.compose.material3.Slider(
            value = displayTimeMs.toFloat(),
            onValueChange = {}, // read-only in this decoupled view
            valueRange = 0f..totalTimeMs.toFloat().coerceAtLeast(1f),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = primaryColor,
                inactiveTrackColor = primaryColor.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(16.dp)
                .testTag("song_time_slider")
        )
        
        Text(
            text = String.format("%02d:%02d", totalTimeMs / 60000, (totalTimeMs / 1000) % 60),
            color = primaryColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
