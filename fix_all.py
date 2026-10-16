import re

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Fix widthIn typo
text = text.replace("import androidx.compose.foundation.layout.wrapContentWidthIn", "import androidx.compose.foundation.layout.widthIn")
text = text.replace("import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.wrapContentWidth\nimport androidx.compose.foundation.layout.widthIn", "import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.wrapContentWidth\nimport androidx.compose.foundation.layout.widthIn")
# Just to be sure widthIn is imported and wrapContentWidthIn is gone:
if "wrapContentWidthIn" in text:
    text = text.replace("wrapContentWidthIn", "widthIn")
if "import androidx.compose.foundation.layout.widthIn" not in text:
    text = text.replace("import androidx.compose.foundation.layout.width", "import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.widthIn")

# Fix exhaustive whens
text = re.sub(
    r"(when\s*\(activity\)\s*\{\n\s*is LiveActivityState\.Music -> \{\n\s*if\s*\(settings\.mediaDisplayMode == MediaDisplayMode\.WAVEFORM_ONLY\))",
    r"when (activity) {\n            is LiveActivityState.Idle -> {}\n            is LiveActivityState.Music -> {\n                if (settings.mediaDisplayMode == MediaDisplayMode.WAVEFORM_ONLY)",
    text
)

text = re.sub(
    r"(when\s*\(activity\)\s*\{\n\s*is LiveActivityState\.Music -> \{\n\s*if\s*\(settings\.mediaDisplayMode == MediaDisplayMode\.COVER_ONLY\))",
    r"when (activity) {\n            is LiveActivityState.Idle -> {}\n            is LiveActivityState.Music -> {\n                if (settings.mediaDisplayMode == MediaDisplayMode.COVER_ONLY)",
    text
)

text = re.sub(
    r"(when\s*\(activity\)\s*\{\n\s*is LiveActivityState\.Music -> \{\n\s*ExpandedMusicPlayer\()",
    r"when (activity) {\n            is LiveActivityState.Idle -> {}\n            is LiveActivityState.Music -> {\n                ExpandedMusicPlayer(",
    text
)

# Fix RealtimeWaveformView arguments
text = re.sub(
    r"RealtimeWaveformView\(\s*isPlaying = activity\.isPlaying,\s*primaryColor = activity\.song\.primaryColor,\s*secondaryColor = activity\.song\.secondaryColor,\s*barCount = \d+,\s*isSmoothMode = settings\.enableHyperSmoothMode,?\s*\)",
    r"RealtimeWaveformView(isPlaying = activity.isPlaying, barColor = activity.song.primaryColor)",
    text,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "r") as f:
    text = f.read()

# Fix exhaustive when in ViewModel
text = text.replace(
"""        val secondaryRunning = when (secondary) {
            is LiveActivityState.Music -> secondary.isPlaying""",
"""        val secondaryRunning = when (secondary) {
            is LiveActivityState.Idle -> false
            is LiveActivityState.Music -> secondary.isPlaying"""
)

with open("app/src/main/java/com/example/viewmodel/DynamicIslandViewModel.kt", "w") as f:
    f.write(text)
