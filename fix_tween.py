import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

if "import androidx.compose.animation.core.LinearOutSlowInEasing" not in text:
    text = text.replace("import androidx.compose.animation.core.LinearEasing", "import androidx.compose.animation.core.LinearEasing\nimport androidx.compose.animation.core.LinearOutSlowInEasing")

text = text.replace(
    "tween(durationMillis = 200, easing = FastOutSlowInEasing)",
    "tween(durationMillis = 180, easing = LinearOutSlowInEasing)"
)

# And replace animateContentSize without specific tween if any, wait, it has it explicitly.

# Fix graphicsLayer
text = text.replace(
    ".testTag(\"dynamic_island_pill\")",
    ".testTag(\"dynamic_island_pill\")\n                .graphicsLayer { clip = true; shape = RoundedCornerShape(cornerRadius) }"
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
