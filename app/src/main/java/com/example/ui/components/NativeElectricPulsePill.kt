package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AuraLightingEffect

@Composable
fun NativeElectricPulsePill(
    isTriggered: Boolean,
    isAmbientPulsing: Boolean = false,
    albumColor: Color = Color(0xFF00E5FF),
    auraEffect: AuraLightingEffect = AuraLightingEffect.HYBRID_PREMIUM,
    specularSheenEnabled: Boolean = true,
    glowIntensity: Float = 0.45f,
    widthDp: Dp = 200.dp,
    heightDp: Dp = 36.dp,
    onAnimationEnd: () -> Unit = {}
) {
    val animatedAccentColor by animateColorAsState(
        targetValue = albumColor,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "pill_color_transition"
    )

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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loop")

    // A. Dual-Phase Organic Breathing Halo
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )

    // B. Sonic Beat Shockwave Ping
    val pingAnimatable = remember { Animatable(0f) }
    LaunchedEffect(albumColor) {
        if (auraEffect == AuraLightingEffect.SONIC_BEAT_PING || auraEffect == AuraLightingEffect.HYBRID_PREMIUM) {
            pingAnimatable.snapTo(0f)
            pingAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(650, easing = FastOutLinearInEasing)
            )
        }
    }

    // C. Specular Glass Edge Sheen
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -250f,
        targetValue = 700f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, delayMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BorderSheen"
    )

    val specularBorderBrush = if (specularSheenEnabled) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.12f)
            ),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 140f, 100f)
        )
    } else {
        SolidColor(Color.White.copy(alpha = 0.12f))
    }

    val progress = animProgress.value
    val pillShape = remember { RoundedCornerShape(28.dp) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAmbientPulsing) {
            val baseIntensity = glowIntensity.coerceIn(0.15f, 0.80f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        alpha = haloAlpha * baseIntensity
                        clip = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                32f, 32f, android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        } else {
                            shadowElevation = 24f
                        }
                    }
                    .background(animatedAccentColor.copy(alpha = 0.45f), shape = pillShape)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val pingVal = pingAnimatable.value
                        if (pingVal in 0.01f..0.99f) {
                            scaleX = 1.0f + (pingVal * 0.30f)
                            scaleY = 1.0f + (pingVal * 0.30f)
                            alpha = (1f - pingVal) * 0.65f * (baseIntensity / 0.40f)
                        } else {
                            alpha = 0f
                        }
                        clip = false
                    }
                    .border(
                        width = 2.dp,
                        color = animatedAccentColor,
                        shape = pillShape
                    )
            )
        }

        Surface(
            shape = pillShape,
            color = Color(0xFF0D0D11),
            border = BorderStroke(1.dp, specularBorderBrush),
            modifier = Modifier
                .width(widthDp)
                .height(heightDp)
                .graphicsLayer {
                    translationY = (1f - progress) * 800f
                    alpha = 0.3f + (progress * 0.7f)
                }
        ) {
            // Content container
        }
    }
}
