package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 16.dp,
    barCount: Int = 4,
    isSmoothMode: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val staticScale = if (isPlaying) 0.6f else 0.25f
    val h1 = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 = transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 290, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 = transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 410, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )
    
    Canvas(modifier = modifier) {
        val scaleFactors = if (isSmoothMode || h1 == null || h2 == null || h3 == null || h4 == null) floatArrayOf(staticScale, staticScale*0.8f, staticScale*1.2f, staticScale*0.9f) else floatArrayOf(h1.value, h2.value, h3.value, h4.value)
        val totalSpacing = (barCount - 1) * 2.dp.toPx()
        val bw = barWidth.toPx()
        val mh = maxHeight.toPx()
        
        for (i in 0 until barCount) {
            val scale = if (isPlaying) scaleFactors[i % scaleFactors.size] else 0.25f
            val h = mh * scale
            val x = i * (bw + 2.dp.toPx())
            val y = (size.height - h) / 2f
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(bw, h),
                cornerRadius = CornerRadius(bw / 2, bw / 2)
            )
        }
    }
}
