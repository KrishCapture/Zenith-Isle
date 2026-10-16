with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

start_idx = text.find("// Expanded Music Player Screen")
end_idx = text.find("// Expanded Workout Tracking View")

new_func = """// Expanded Music Player Screen
@Composable
private fun ExpandedMusicPlayer(
    state: LiveActivityState.Music,
    viewModel: DynamicIslandViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val song = state.song
    val songTimeMs = state.positionMs
    val totalTimeMs = song.durationMs

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation_angle"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (settings.mediaDisplayMode == MediaDisplayMode.WAVEFORM_ONLY) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10121C))
                            .border(1.dp, song.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RealtimeWaveformView(
                            isPlaying = state.isPlaying,
                            primaryColor = song.primaryColor,
                            secondaryColor = song.secondaryColor,
                            barCount = 10,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                remember(song.primaryColor, song.secondaryColor) {
                                    Brush.linearGradient(
                                        listOf(song.primaryColor, song.secondaryColor)
                                    )
                                }
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = song.coverBitmap ?: song.coverArtUrl,
                            contentDescription = "${song.album} Cover Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(if (state.isPlaying) rotationAngle else 0f)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Title, Artist, Audio Quality
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = song.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = song.primaryColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = song.audioQuality,
                        color = song.primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.testTag("music_favorite_button")
            ) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (state.isFavorite) Color(0xFFEC4899) else TextMuted
                )
            }
        }

        if (settings.mediaDisplayMode == MediaDisplayMode.BOTH || settings.mediaDisplayMode == MediaDisplayMode.WAVEFORM_ONLY) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131522))
                    .border(1.dp, song.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                RealtimeWaveformView(
                    isPlaying = state.isPlaying,
                    primaryColor = song.primaryColor,
                    secondaryColor = song.secondaryColor,
                    barCount = 28,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Song Time Progress Slider
        Column {
            Slider(
                value = songTimeMs.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..totalTimeMs.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = song.primaryColor,
                    inactiveTrackColor = Color(0xFF1E2130)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .testTag("song_time_slider")
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMins = (songTimeMs / 1000) / 60
                val currentSecs = (songTimeMs / 1000) % 60
                val remainingMs = (totalTimeMs - songTimeMs).coerceAtLeast(0L)
                val remMins = (remainingMs / 1000) / 60
                val remSecs = (remainingMs / 1000) % 60

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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.isShuffle) song.primaryColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { viewModel.skipPreviousSong() },
                modifier = Modifier.testTag("skip_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        remember(song.primaryColor, song.secondaryColor) {
                            Brush.linearGradient(
                                listOf(song.primaryColor, song.secondaryColor)
                            )
                        }
                    )
                    .clickable { viewModel.togglePlayPause() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { viewModel.skipNextSong() },
                modifier = Modifier.testTag("skip_next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { viewModel.toggleRepeat() },
                modifier = Modifier.testTag("repeat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (state.isRepeat) song.primaryColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text[:start_idx] + new_func + text[end_idx:])

