    val targetPrimaryColor = remember(activeActivity, dynamicPaletteColor) {
        when (activeActivity) {
            is LiveActivityState.Idle -> Color.Transparent
            is LiveActivityState.Music -> dynamicPaletteColor ?: activeActivity.song.primaryColor
            is LiveActivityState.Workout -> Color(0xFF10B981)
            is LiveActivityState.Timer -> Color(0xFFF59E0B)
            is LiveActivityState.Navigation -> Color(0xFF3B82F6)
            is LiveActivityState.Delivery -> Color(0xFFEC4899)
            is LiveActivityState.IncomingCall -> Color(0xFF10B981)
        }
    }

    val animatedAccentColor by androidx.compose.animation.animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600, // Smooth 600ms transition delay
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "pill_color_transition"
    )

    val glowBrush = remember(settings.enableGlow, animatedAccentColor, isExpanded) {
        if (settings.enableGlow) {
            Brush.horizontalGradient(
                colors = listOf(
                    animatedAccentColor.copy(alpha = if (isExpanded) 0.5f else 0.25f),
                    SecondaryNeon.copy(alpha = if (isExpanded) 0.4f else 0.15f),
                    animatedAccentColor.copy(alpha = if (isExpanded) 0.5f else 0.25f)
                )
            )
        } else {
            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
        }
    }
