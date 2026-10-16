with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

# Fix the imports
import re
text = re.sub(
    r"import androidx\.compose\.material\.icons\.filled\.HeadphonesBorder",
    "import androidx.compose.material.icons.filled.FavoriteBorder",
    text
)

# And remove duplicate @Composable on DecoupledPlaybackTimeDisplay if any
# Wait, the error said "This annotation is not repeatable" at line 1577
text = text.replace("@Composable\n@Composable\nprivate fun DecoupledPlaybackTimeDisplay", "@Composable\nprivate fun DecoupledPlaybackTimeDisplay")

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
