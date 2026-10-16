import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

if "import androidx.compose.foundation.interaction.MutableInteractionSource" not in text:
    text = text.replace("import androidx.compose.foundation.clickable", "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.interaction.MutableInteractionSource")

text = re.sub(
    r"\.clickable\s*\{\s*(.*?)\s*\}",
    r".clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { \1 }",
    text
)

# Fix the multi-line clickable at 250
text = re.sub(
    r"\.clickable\(\s*interactionSource\s*=\s*remember\s*\{\s*MutableInteractionSource\(\)\s*\},\s*indication\s*=\s*null\s*\)\s*\{\s*if\s*\(isExpanded\)\s*\{\s*viewModel\.setExpansionMode\(IslandExpansionMode\.COMPACT\)\s*\}\s*else\s*\{\s*viewModel\.setExpansionMode\(IslandExpansionMode\.EXPANDED\)\s*\}\s*\}",
    ".clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isExpanded) { viewModel.setExpansionMode(IslandExpansionMode.COMPACT) } else { viewModel.setExpansionMode(IslandExpansionMode.EXPANDED) } }",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
