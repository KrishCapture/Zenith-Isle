with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

text = re.sub(
    r"\.padding\(horizontal = 12\.dp, vertical = 8\.dp\)",
    """.padding(
                start = 10.dp,
                end = if (activeActivity is LiveActivityState.Music) 4.dp else 10.dp,
                top = 6.dp,
                bottom = 6.dp
            )""",
    text
)

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
