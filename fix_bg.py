with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Restore the split pill
text = text.replace(
    """.background(
                    if (isExpanded && activeActivity is LiveActivityState.Music) 
                        Brush.verticalGradient(
                            listOf(
                                (activeActivity as LiveActivityState.Music).song.primaryColor.copy(alpha = 0.6f),
                                islandBgColor,
                                islandBgColor
                            )
                        )
                    else 
                        androidx.compose.ui.graphics.SolidColor(islandBgColor)
                )
                    .border(1.dp, SecondaryNeon.copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.disableSplitMode() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("split_pill_right")""",
    """.background(islandBgColor)
                    .border(1.dp, SecondaryNeon.copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.disableSplitMode() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("split_pill_right")"""
)

# Replace the main pill background
text = text.replace(
    """.animateContentSize(animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing))
                .background(islandBgColor)
                .border(
                    width = if (isExpanded) 1.5.dp else 1.dp,""",
    """.animateContentSize(animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing))
                .background(
                    if (isExpanded && activeActivity is LiveActivityState.Music) 
                        Brush.verticalGradient(
                            listOf(
                                (activeActivity as LiveActivityState.Music).song.primaryColor.copy(alpha = 0.4f),
                                islandBgColor,
                                islandBgColor
                            )
                        )
                    else 
                        androidx.compose.ui.graphics.SolidColor(islandBgColor)
                )
                .border(
                    width = if (isExpanded) 1.5.dp else 1.dp,"""
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
