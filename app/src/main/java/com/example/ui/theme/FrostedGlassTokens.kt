package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object FrostedGlassTokens {
    // Light Frosted Pill & Card Surface
    val pillBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.88f),
            Color(0xFFF2F2F7).copy(alpha = 0.72f)
        )
    )
    val cardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.65f),
            Color(0xFFEDEDF2).copy(alpha = 0.45f)
        )
    )
    val border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f))
    val innerHighlight = BorderStroke(0.5.dp, Color(0xFF000000).copy(alpha = 0.06f))
    val textPrimary = Color(0xFF1C1C1E)
    val textSecondary = Color(0xFF636366)
    val sliderTrackInactive = Color(0xFF000000).copy(alpha = 0.08f)
}
