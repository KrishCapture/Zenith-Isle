                } else {
                    // Animated Equalizer Bars on right
                    EqualizerBars(
                        isPlaying = activity.isPlaying,
                        color = accentColor,
                        barCount = 4,
                        maxHeight = 10.dp,
                        isSmoothMode = settings.enableHyperSmoothMode,
                        modifier = Modifier.offset(x = (-15).dp)
                    )
                }
