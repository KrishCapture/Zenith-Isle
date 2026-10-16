sed -i '210,217c\
            // Settings Sheet\
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {\
                SettingsDrawer(\
                    settings = settings,\
                    viewModel = viewModel\
                )\
            }\
' app/src/main/java/com/example/MainActivity.kt
sed -i '217,222d' app/src/main/java/com/example/MainActivity.kt
