with open("app/src/main/java/com/example/ui/components/EqualizerBar.kt", "r") as f:
    text = f.read()

import re
new_text = re.sub(
    r"val transition = rememberInfiniteTransition\(label = \"equalizer\"\)\s*val h1 = transition\.animateFloat\([\s\S]*?label = \"h4\"\n    \)",
    """
    val h1 = if (isSmoothMode) null else rememberInfiniteTransition(label = "eq1").animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(350, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "h1"
    )
    val h2 = if (isSmoothMode) null else rememberInfiniteTransition(label = "eq2").animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(animation = tween(480, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "h2"
    )
    val h3 = if (isSmoothMode) null else rememberInfiniteTransition(label = "eq3").animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(290, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "h3"
    )
    val h4 = if (isSmoothMode) null else rememberInfiniteTransition(label = "eq4").animateFloat(
        initialValue = 0.7f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(410, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "h4"
    )""",
    text
)

new_text = new_text.replace(
    "val scaleFactors = if (isSmoothMode) floatArrayOf(staticScale, staticScale*0.8f, staticScale*1.2f, staticScale*0.9f) else floatArrayOf(h1.value, h2.value, h3.value, h4.value)",
    "val scaleFactors = if (isSmoothMode || h1 == null || h2 == null || h3 == null || h4 == null) floatArrayOf(staticScale, staticScale*0.8f, staticScale*1.2f, staticScale*0.9f) else floatArrayOf(h1.value, h2.value, h3.value, h4.value)"
)

with open("app/src/main/java/com/example/ui/components/EqualizerBar.kt", "w") as f:
    f.write(new_text)
