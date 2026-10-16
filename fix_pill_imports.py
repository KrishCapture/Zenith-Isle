import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Fix the import typo
text = text.replace("import androidx.compose.foundation.layout.wrapContentWidthIn", "import androidx.compose.foundation.layout.widthIn")
text = text.replace("import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.wrapContentWidth\nimport androidx.compose.foundation.layout.widthIn", "import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.wrapContentWidth\nimport androidx.compose.foundation.layout.widthIn")

# Let's fix exhaustive whens in DynamicIslandPill.kt
text = re.sub(
    r"(when\s*\(activeActivity\)\s*\{)",
    r"\1\n            is LiveActivityState.Idle -> Color.Transparent",
    text,
    count=1 # The primaryColor one
)

text = text.replace(
"""        when (activity) {
            is LiveActivityState.Music -> {
                if (settings.mediaDisplayMode == MediaDisplayMode.WAVEFORM_ONLY) {""",
"""        when (activity) {
            is LiveActivityState.Idle -> {}
            is LiveActivityState.Music -> {
                if (settings.mediaDisplayMode == MediaDisplayMode.WAVEFORM_ONLY) {"""
)

text = text.replace(
"""        when (activity) {
            is LiveActivityState.Music -> {
                if (settings.mediaDisplayMode == MediaDisplayMode.COVER_ONLY) {""",
"""        when (activity) {
            is LiveActivityState.Idle -> {}
            is LiveActivityState.Music -> {
                if (settings.mediaDisplayMode == MediaDisplayMode.COVER_ONLY) {"""
)

text = text.replace(
"""        when (activity) {
            is LiveActivityState.Music -> {
                ExpandedMusicPlayer(""",
"""        when (activity) {
            is LiveActivityState.Idle -> {}
            is LiveActivityState.Music -> {
                ExpandedMusicPlayer("""
)

# And fix line 195: `Unresolved reference 'copy'` in DynamicIslandPill.kt? Wait, what's on line 195?
