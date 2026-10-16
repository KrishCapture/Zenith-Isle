import re

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "r") as f:
    text = f.read()

text = text.replace(
    "WindowManager.LayoutParams.WRAP_CONTENT,\n            WindowManager.LayoutParams.WRAP_CONTENT,",
    "WindowManager.LayoutParams.MATCH_PARENT,\n            WindowManager.LayoutParams.WRAP_CONTENT,"
)

text = re.sub(
    r"setContent \{\n\s*MyApplicationTheme \{\n\s*val activeActivity = viewModel\.activeActivity\.collectAsState\(\)\.value[\s\S]*?viewModel = viewModel\n\s*\)\n\s*\}\n\s*\}",
    """setContent {
                MyApplicationTheme {
                    val activeActivity = viewModel.activeActivity.collectAsState().value
                    val secondaryActivity = viewModel.secondaryActivity.collectAsState().value
                    val expansionMode = viewModel.expansionMode.collectAsState().value
                    val settings = viewModel.settings.collectAsState().value
                    
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(top = settings.yOffsetDp.dp),
                        contentAlignment = androidx.compose.ui.Alignment.TopCenter
                    ) {
                        DynamicIslandPill(
                            activeActivity = activeActivity,
                            secondaryActivity = secondaryActivity,
                            expansionMode = expansionMode,
                            settings = settings,
                            viewModel = viewModel
                        )
                    }
                }
            }""",
    text
)

# Also import fillMaxWidth, height, padding, dp
text = text.replace("import androidx.compose.ui.platform.LocalDensity", "import androidx.compose.ui.platform.LocalDensity\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.unit.dp")

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "w") as f:
    f.write(text)
