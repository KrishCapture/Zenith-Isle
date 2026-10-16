sed -i '234,245c\
                .graphicsLayer {\
                    shadowElevation = if (isExpanded) 20.dp.toPx() else 8.dp.toPx()\
                    shape = RoundedCornerShape(cornerRadius)\
                    ambientShadowColor = primaryColor.copy(alpha = 0.4f)\
                    spotShadowColor = primaryColor.copy(alpha = 0.6f)\
                    clip = true\
                }\
' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
