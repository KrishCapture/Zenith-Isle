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

start, end = get_block(text, "private fun ExpandedMusicPlayer(")
if start != -1:
    new_func = """private fun ExpandedMusicPlayer(
    state: LiveActivityState.Music,
    viewModel: DynamicIslandViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val song = state.song
    val songTimeMs = state.positionMs
    val totalTimeMs = song.durationMs

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Top Row: Album Art, Title/Artist, Headphone Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Album Art with small icon overlay
                Box(
                    modifier = Modifier.size(64.dp)
                ) {
                    AsyncImage(
                        model = song.coverBitmap ?: song.coverArtUrl,
                        contentDescription = "${song.album} Cover Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                    )
                    // Small overlay icon (Music note)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFFA243C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                // Title and Artist
                Column(
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Headphone Icon
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = "Headphones",
                tint = Color.White,
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar Row (Time - Slider - Time)
        DecoupledPlaybackTimeDisplay(songTimeMs = songTimeMs, totalTimeMs = totalTimeMs, primaryColor = song.primaryColor)

        Spacer(modifier = Modifier.height(8.dp))

        // Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like (Favorite)
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.testTag("music_favorite_button")
            ) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            IconButton(
                onClick = { viewModel.skipPreviousSong() },
                modifier = Modifier.testTag("skip_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause (No background circle, just the icon)
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(
                onClick = { viewModel.skipNextSong() },
                modifier = Modifier.testTag("skip_next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Shuffle
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.isShuffle) song.primaryColor else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
"""
    text = text[:start] + new_func + text[end:]
    with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
        f.write(text)
    print("Replaced ExpandedMusicPlayer")
else:
    print("Could not find ExpandedMusicPlayer")
