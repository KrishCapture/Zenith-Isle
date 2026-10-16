cat > replace.txt << 'INNER'
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
INNER
sed -i '888,901d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '887r replace.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
