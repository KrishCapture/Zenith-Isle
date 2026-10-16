sed -i '147,156c\
    val bgOpacity = remember(settings.enableDynamicTheme, isDarkSystem, settings.darkBgOpacity, settings.lightBgOpacity) {\
        if (settings.enableDynamicTheme) {\
            if (isDarkSystem) settings.darkBgOpacity else settings.lightBgOpacity\
        } else {\
            settings.darkBgOpacity\
        }\
    }\
    val blurLevelDp = remember(settings.enableDynamicTheme, isDarkSystem, settings.darkBlurLevelDp, settings.lightBlurLevelDp) {\
        if (settings.enableDynamicTheme) {\
            if (isDarkSystem) settings.darkBlurLevelDp else settings.lightBlurLevelDp\
        } else {\
            settings.darkBlurLevelDp\
        }\
    }\
' app/src/main/java/com/example/ui/components/DynamicIslandPill.kt
