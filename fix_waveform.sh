cat > replace.txt << 'INNER'
@Composable
fun RealtimeWaveformView(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 20
) {
    val infiniteTransition = rememberInfiniteTransition(label = "realtime_waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )

    val waveBrush = remember(primaryColor, secondaryColor) {
        Brush.verticalGradient(colors = listOf(primaryColor, secondaryColor))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val totalSpacing = width * 0.25f
        val barCountSafe = barCount.coerceAtLeast(1)
        val barWidth = (width - totalSpacing) / barCountSafe
        val gap = if (barCountSafe > 1) totalSpacing / (barCountSafe - 1) else 0f

        for (i in 0 until barCountSafe) {
            val progress = i.toFloat() / (barCountSafe - 1).coerceAtLeast(1).toFloat()
            val wave1 = kotlin.math.sin(phase + progress * 9f).toFloat()
            val wave2 = kotlin.math.cos(phase * 1.5f + progress * 13f).toFloat()
            val envelope = kotlin.math.sin(progress * Math.PI.toFloat()).toFloat().coerceAtLeast(0.35f)
            
            val rawAmplitude = ((wave1 * 0.45f + wave2 * 0.35f + 0.5f) * envelope)
            val amplitude = if (isPlaying) rawAmplitude.coerceIn(0.18f, 1f) else 0.12f
            
            val barHeight = height * amplitude
            val x = i * (barWidth + gap)
            val y = (height - barHeight) / 2f
            
            drawRoundRect(
                brush = waveBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
INNER
sed -i '1388,1447d' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
sed -i '1387r replace.txt' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
