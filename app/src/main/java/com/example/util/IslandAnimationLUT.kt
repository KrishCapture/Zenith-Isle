package com.example.util

data class PulseMorphBounds(
    val width: Float,
    val height: Float,
    val yOffset: Float,
    val cornerRadius: Float,
    val pulseAlpha: Float,
    val pulseScale: Float
)

object IslandAnimationLUT {

    // Your exact pre-computed 13-step easing curve array (0ms to 180ms @ 60fps)
    private val EASE_OUT_STEPS = floatArrayOf(
        0f, 0.0139f, 0.0556f, 0.125f, 0.2222f, 0.3472f, 
        0.5f, 0.6528f, 0.7778f, 0.875f, 0.9444f, 0.9861f, 1f
    )

    /**
     * Look up the interpolated value directly from the pre-calculated array.
     * Zero math, zero physics engine overhead, zero GC allocations.
     */
    fun getInterpolatedValue(startDp: Float, targetDp: Float, progressFraction: Float): Float {
        if (progressFraction <= 0f) return startDp
        if (progressFraction >= 1f) return targetDp

        val lastIndex = EASE_OUT_STEPS.size - 1
        val rawIndex = progressFraction * lastIndex
        val index = rawIndex.toInt().coerceIn(0, lastIndex - 1)
        val remainder = rawIndex - index

        // Lightweight linear interpolation between pre-calculated keyframe steps
        val stepStart = EASE_OUT_STEPS[index]
        val stepEnd = EASE_OUT_STEPS[index + 1]
        val curveValue = stepStart + (stepEnd - stepStart) * remainder

        return startDp + (targetDp - startDp) * curveValue
    }

    fun getBottomToTopPulseBounds(
        screenHeightDp: Float = 800f, // Screen bottom starting point
        targetPillYDp: Float = 8f,     // Camera notch position
        progressFraction: Float
    ): PulseMorphBounds {
        val fraction = progressFraction.coerceIn(0f, 1f)

        // Travel from bottom of screen (screenHeightDp) to top notch (targetPillYDp)
        val currentY = getInterpolatedValue(screenHeightDp, targetPillYDp, fraction)
        
        // Morph size from wide app card down to compact pill size
        val currentWidth = getInterpolatedValue(320f, 200f, fraction)
        val currentHeight = getInterpolatedValue(70f, 36f, fraction)
        
        // Morph corners from standard phone card (16dp) to full capsule (28dp)
        val cornerRadius = getInterpolatedValue(16f, 28f, fraction)
        
        // Electric pulse trail brightness & scale squeeze during travel
        val pulseAlpha = getInterpolatedValue(0.3f, 1.0f, fraction)
        val pulseScale = getInterpolatedValue(1.15f, 1.0f, fraction)

        return PulseMorphBounds(
            width = currentWidth,
            height = currentHeight,
            yOffset = currentY,
            cornerRadius = cornerRadius,
            pulseAlpha = pulseAlpha,
            pulseScale = pulseScale
        )
    }
}

