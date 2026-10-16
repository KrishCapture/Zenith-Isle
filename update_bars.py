import re

with open("app/src/main/java/com/example/ui/components/EqualizerBar.kt", "r") as f:
    eq_text = f.read()

eq_text = eq_text.replace(
    "barCount: Int = 4\n)",
    "barCount: Int = 4,\n    isSmoothMode: Boolean = false\n)"
)

# If smooth mode, maybe we don't animate or we animate less?
eq_text = eq_text.replace(
    "val transition = rememberInfiniteTransition(label = \"equalizer\")",
    """val transition = rememberInfiniteTransition(label = "equalizer")
    val staticScale = if (isPlaying) 0.6f else 0.25f"""
)

eq_text = eq_text.replace(
    "val scaleFactors = floatArrayOf(h1.value, h2.value, h3.value, h4.value)",
    "val scaleFactors = if (isSmoothMode) floatArrayOf(staticScale, staticScale*0.8f, staticScale*1.2f, staticScale*0.9f) else floatArrayOf(h1.value, h2.value, h3.value, h4.value)"
)

with open("app/src/main/java/com/example/ui/components/EqualizerBar.kt", "w") as f:
    f.write(eq_text)
