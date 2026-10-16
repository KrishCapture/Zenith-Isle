sed -i 's/Brush.linearGradient(/remember(activity.song.primaryColor, activity.song.secondaryColor) { Brush.linearGradient(/g' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i 's/listOf(activity.song.primaryColor, activity.song.secondaryColor)/listOf(activity.song.primaryColor, activity.song.secondaryColor) }/g' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt

sed -i 's/Brush.linearGradient(/remember(song.primaryColor, song.secondaryColor) { Brush.linearGradient(/g' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i 's/listOf(song.primaryColor, song.secondaryColor)/listOf(song.primaryColor, song.secondaryColor) }/g' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
