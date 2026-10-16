cat > replace.txt << 'INNER'
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
INNER
sed -i '691,704d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '690r replace.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
