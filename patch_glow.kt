        // Ambient Glow behind the pill (when playing music)
        if (activeActivity is LiveActivityState.Music && activeActivity.isPlaying) {
            val infiniteTransition = rememberInfiniteTransition()
            val breathingScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing_scale"
            )
            Box(
                modifier = Modifier
                    .width(animatedWidth).defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    .height(if (isHidden) 1.dp else if (isExpanded) 200.dp else 40.dp)
                    .blur(40.dp).graphicsLayer {
                        scaleX = breathingScale
                        scaleY = breathingScale * 1.1f
                        alpha = 0.5f
                        translationY = entranceOffsetY.value
                    }
                    .background(animatedAccentColor, morphingShape)
            )
        }
