cat > replace.txt << 'INNER'
    val bgOpacity = remember(settings.enableDynamicTheme, isDarkSystem, settings.darkBgOpacity, settings.lightBgOpacity) {
        if (settings.enableDynamicTheme) {
            if (isDarkSystem) settings.darkBgOpacity else settings.lightBgOpacity
        } else {
            settings.darkBgOpacity
        }
    }
    val blurLevelDp = remember(settings.enableDynamicTheme, isDarkSystem, settings.darkBlurLevelDp, settings.lightBlurLevelDp) {
        if (settings.enableDynamicTheme) {
            if (isDarkSystem) settings.darkBlurLevelDp else settings.lightBlurLevelDp
        } else {
            settings.darkBlurLevelDp
        }
    }

    val islandBgColor = remember(settings.enableDynamicTheme, isDarkSystem, bgOpacity) {
        if (settings.enableDynamicTheme && !isDarkSystem) {
            Color(0xFF0F1017).copy(alpha = bgOpacity)
        } else {
            IslandBlack.copy(alpha = bgOpacity)
        }
    }

    val primaryColor = remember(activeActivity) {
        when (activeActivity) {
            is LiveActivityState.Music -> activeActivity.song.primaryColor
            is LiveActivityState.Workout -> Color(0xFF10B981)
            is LiveActivityState.Timer -> Color(0xFFF59E0B)
            is LiveActivityState.Navigation -> Color(0xFF3B82F6)
            is LiveActivityState.Delivery -> Color(0xFFEC4899)
            is LiveActivityState.IncomingCall -> Color(0xFF10B981)
        }
    }

    val glowBrush = remember(settings.enableGlow, primaryColor, isExpanded) {
        if (settings.enableGlow) {
            Brush.horizontalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (isExpanded) 0.5f else 0.25f),
                    SecondaryNeon.copy(alpha = if (isExpanded) 0.4f else 0.15f),
                    primaryColor.copy(alpha = if (isExpanded) 0.5f else 0.25f)
                )
            )
        } else {
            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
        }
    }
INNER
