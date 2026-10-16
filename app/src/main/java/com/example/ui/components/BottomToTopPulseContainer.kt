package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.util.IslandAnimationLUT

@Composable
fun ZeroLagBottomToTopPulse(
    isTriggeredFromBottom: Boolean,
    albumColor: Color,
    screenHeightPx: Float = 1800f, // Starting travel distance in pixels
    onDockCompleted: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // 0f = Screen Bottom, 1f = Docked at Top Camera Notch
    val pulseProgress = remember { Animatable(0f) }

    LaunchedEffect(isTriggeredFromBottom) {
        if (isTriggeredFromBottom) {
            pulseProgress.snapTo(0f)
            pulseProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200) // Fast 200ms LUT sweep
            )
            onDockCompleted()
        } else {
            pulseProgress.snapTo(1f)
        }
    }

    // Read lookup value without allocating new objects
    val progressFraction = pulseProgress.value
    val lutStep = remember(progressFraction) {
        IslandAnimationLUT.getInterpolatedValue(0f, 1f, progressFraction)
    }

    // Fixed, pre-allocated capsule shape (Zero GC memory allocations during animation)
    val fixedPillShape = remember { RoundedCornerShape(28.dp) }
    val electricBorder = remember(albumColor) { 
        BorderStroke(1.5.dp, SolidColor(albumColor)) 
    }

    Box(
        modifier = Modifier
            .width(200.dp)  // Fixed layout bounds to avoid CPU re-measuring
            .height(36.dp)
            .graphicsLayer {
                // Hardware GPU Translation (Zero layout passes on CPU)
                translationY = (1f - lutStep) * screenHeightPx
                
                // Lightweight scale pulse effect during flight
                val scale = 1f + ((1f - lutStep) * 0.12f)
                scaleX = scale
                scaleY = scale
                
                // Fade in as it approaches top
                alpha = 0.3f + (lutStep * 0.7f)
            }
            .border(electricBorder, fixedPillShape)
            .background(Color.Black, fixedPillShape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun BottomToTopPulseContainer(
    isTriggeredFromBottom: Boolean,
    albumColor: Color, // The extracted album cover color
    screenHeightDp: Float = 800f,
    targetNotchYDp: Float = 8f,
    onDockCompleted: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // 0f = Screen Bottom, 1f = Docked at Top Camera Notch
    val pulseProgress = remember { Animatable(0f) }

    LaunchedEffect(isTriggeredFromBottom) {
        if (isTriggeredFromBottom) {
            pulseProgress.snapTo(0f)
            // Fast 220ms electric fly-up transition mapped through the 13-step LUT
            pulseProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220)
            )
            onDockCompleted()
        } else {
            pulseProgress.snapTo(1f)
        }
    }

    // Look up pre-calculated trajectory points (Zero CPU math per frame)
    val bounds = remember(pulseProgress.value) {
        IslandAnimationLUT.getBottomToTopPulseBounds(
            screenHeightDp = screenHeightDp,
            targetPillYDp = targetNotchYDp,
            progressFraction = pulseProgress.value
        )
    }

    // Pre-allocate the electric pulse gradient border using the album color
    val electricPulseBrush = remember(albumColor) {
        Brush.verticalGradient(
            colors = listOf(
                albumColor.copy(alpha = 0.95f),
                albumColor.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.80f) // Electric core spark line
            )
        )
    }

    // Pre-allocate shape to prevent GC memory thrashing
    val shape = remember(bounds.cornerRadius) {
        RoundedCornerShape(bounds.cornerRadius.dp)
    }

    Box(
        modifier = Modifier
            .offset(y = bounds.yOffset.dp)
            .width(bounds.width.dp)
            .height(bounds.height.dp)
            .graphicsLayer {
                // Apply lightweight scale squeeze to simulate high-speed electrical movement
                scaleX = bounds.pulseScale
                scaleY = bounds.pulseScale
            }
            .alpha(bounds.pulseAlpha)
            // Electric glowing border around the traveling black pill
            .border(
                border = BorderStroke(1.5.dp, electricPulseBrush),
                shape = shape
            )
            .background(Color.Black, shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
