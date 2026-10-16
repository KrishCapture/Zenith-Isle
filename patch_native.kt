package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NativeElectricPulsePill(
    isTriggered: Boolean,
    isAmbientPulsing: Boolean = false,
    albumColor: Color = Color(0xFF00E5FF), // Electric cyan or album color
    widthDp: Dp = 200.dp,
    heightDp: Dp = 36.dp,
    onAnimationEnd: () -> Unit = {}
) {
    val animatedAccentColor by animateColorAsState(
        targetValue = albumColor,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "pill_color_transition"
    )

    // Progress fraction: 0f = Flying up / pulsing, 1f = Solid black docked pill
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220)
            )
            onAnimationEnd()
        }
    }

    val progress = animProgress.value
    val pillShape = remember { RoundedCornerShape(28.dp) }

    // Pre-allocated electric gradient border (no reflection/gloss)
    val electricBorder = remember(animatedAccentColor, progress) {
        val borderAlpha = (1f - progress) * 0.8f + 0.2f
        Brush.horizontalGradient(
            colors = listOf(
                animatedAccentColor.copy(alpha = borderAlpha),
                Color.White.copy(alpha = (1f - progress) * 0.9f), // Spark line during flight
                animatedAccentColor.copy(alpha = borderAlpha)
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val ambientPulseAlpha = if (isAmbientPulsing) ambientAlpha else 0f

    val ambientGlowBrush = remember(animatedAccentColor) {
        Brush.radialGradient(
            colors = listOf(
                animatedAccentColor,
                Color.Transparent
            )
        )
    }

    Box(
        modifier = Modifier
            .size(widthDp, heightDp)
            .graphicsLayer {
                // GPU-accelerated travel path from bottom of screen to top camera cutout
                translationY = (1f - progress) * 800f
                scaleX = 1f + ((1f - progress) * 0.2f)
                scaleY = 1f - ((1f - progress) * 0.1f) // Squeeze effect during electrical movement
                alpha = 0.3f + (progress * 0.7f)
            }
            .border(BorderStroke(1.5.dp, electricBorder), pillShape)
            .background(Color.Black, pillShape), // Solid matte black interior
        contentAlignment = Alignment.Center
    ) {
        // Ambient pulse layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = ambientPulseAlpha
                }
                .background(ambientGlowBrush, pillShape)
        )
        // Your compact pill content goes here
    }
}
