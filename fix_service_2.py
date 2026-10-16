import re

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "r") as f:
    text = f.read()

text = re.sub(
    r"val density = LocalDensity\.current\s*LaunchedEffect\(settings\.yOffsetDp\) \{\s*try \{\s*params\.y = \(settings\.yOffsetDp \* density\.density\)\.toInt\(\)\s*windowManager\?\.updateViewLayout\(overlayView, params\)\s*\} catch \(e: Exception\) \{\s*e\.printStackTrace\(\)\s*\}\s*\}",
    "",
    text
)

# And remove y = 12 from apply since we handle it in Compose
text = re.sub(
    r"gravity = Gravity\.TOP or Gravity\.CENTER_HORIZONTAL\s*y = 12",
    "gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL\n            y = 0",
    text
)

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "w") as f:
    f.write(text)
