import re
with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

text = text.replace(
    ".background(islandBgColor)",
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
                )"""
)
text = text.replace(
    "color = islandBgColor,\n            shape = RoundedCornerShape(cornerRadius)",
    "color = Color.Transparent,\n            shape = RoundedCornerShape(cornerRadius)"
)
with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
