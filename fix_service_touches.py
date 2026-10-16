import re

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "r") as f:
    text = f.read()

# We need to remove fillMaxWidth() and height(220.dp) to not block touches
text = re.sub(
    r"\.fillMaxWidth\(\)\s*\.height\(220\.dp\)\s*\.padding\(top = settings\.yOffsetDp\.dp\)",
    ".padding(top = settings.yOffsetDp.dp)",
    text
)

with open("app/src/main/java/com/example/service/DynamicIslandOverlayService.kt", "w") as f:
    f.write(text)
