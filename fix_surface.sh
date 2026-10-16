sed -i '229,251c\
        Surface(\
            modifier = modifier\
                .testTag("dynamic_island_pill")\
                .width(animatedWidth)\
                .animateContentSize(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))\
                .graphicsLayer {\
                    shadowElevation = if (isExpanded) 20.dp.toPx() else 8.dp.toPx()\
                    shape = RoundedCornerShape(cornerRadius)\
                    ambientShadowColor = primaryColor.copy(alpha = 0.4f)\
                    spotShadowColor = primaryColor.copy(alpha = 0.6f)\
                    clip = true\
                }\
                .background(islandBgColor)\
                .border(\
                    width = if (isExpanded) 1.5.dp else 1.dp,\
                    brush = glowBrush,\
                    shape = RoundedCornerShape(cornerRadius)\
                )\
                .clickable(\
                    interactionSource = remember { MutableInteractionSource() },\
                    indication = null,\
                    onClick = { viewModel.toggleExpansion() }\
                ),\
            color = islandBgColor,\
            shape = RoundedCornerShape(cornerRadius)\
        ) {\
' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
