cat > replace1.txt << 'INNER'
                            .background(
                                remember(activity.song.primaryColor, activity.song.secondaryColor) {
                                    Brush.linearGradient(
                                        listOf(activity.song.primaryColor, activity.song.secondaryColor)
                                    )
                                }
                            ),
INNER
sed -i '349,353d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '348r replace1.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt

cat > replace2.txt << 'INNER'
                        .background(
                            remember(song.primaryColor, song.secondaryColor) {
                                Brush.linearGradient(
                                    listOf(song.primaryColor, song.secondaryColor)
                                )
                            }
                        )
INNER
sed -i '693,697d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '692r replace2.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt

cat > replace3.txt << 'INNER'
                    .background(
                        remember(song.primaryColor, song.secondaryColor) {
                            Brush.linearGradient(
                                listOf(song.primaryColor, song.secondaryColor)
                            )
                        }
                    )
INNER
sed -i '889,893d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '888r replace3.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
