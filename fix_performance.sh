sed -i 's/\.animateContentSize(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))/.animateContentSize(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))/g' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '/\.graphicsLayer {/,/}/d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
