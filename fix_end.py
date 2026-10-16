import re

with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "r") as f:
    text = f.read()

# I will find "        // Song catalog picker row if Music is active" and everything below until "@Composable\nprivate fun ActivityChip"
text = re.sub(
    r"        // Song catalog picker row if Music is active.*?(?=@Composable\nprivate fun ActivityChip)",
    "        }\n    }\n}\n\n",
    text,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "w") as f:
    f.write(text)

