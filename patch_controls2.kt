            // Like (Favorite)
            PressableIconButton(
                onClick = { viewModel.onUserInteracted(); viewModel.toggleFavorite() },
                modifier = Modifier.testTag("music_favorite_button").padding(8.dp)
            ) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = animatedTextColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            PressableIconButton(
                onClick = { viewModel.onUserInteracted(); viewModel.skipPreviousSong() },
                modifier = Modifier.testTag("skip_prev_button").padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    tint = animatedTextColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause (Animated)
            PressableIconButton(
                onClick = { viewModel.onUserInteracted(); viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(animatedTextColor.copy(alpha = 0.15f))
                    .testTag("play_pause_button")
            ) {
                androidx.compose.animation.Crossfade(targetState = state.isPlaying, label = "play_pause") { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = animatedTextColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Next
            PressableIconButton(
                onClick = { viewModel.onUserInteracted(); viewModel.skipNextSong() },
                modifier = Modifier.testTag("skip_next_button").padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    tint = animatedTextColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Shuffle
            PressableIconButton(
                onClick = { viewModel.onUserInteracted(); viewModel.toggleShuffle() },
                modifier = Modifier.testTag("shuffle_button").padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.isShuffle) animatedTextColor else animatedTextColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
