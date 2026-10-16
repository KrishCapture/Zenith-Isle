import re
with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "r") as f:
    text = f.read()

text = text.replace("        }\n        }\n    }\n}\n\n@Composable", "        }\n    }\n}\n\n@Composable")
text = text.replace("        }\n    }\n}\n\n@Composable", "        }\n    }\n\n@Composable")

with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "w") as f:
    f.write(text)
