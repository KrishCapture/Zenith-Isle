with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

import re

# In CompactLeftContent -> LiveActivityState.Music
text = re.sub(
    r"Text\(\s*text = activity\.song\.title,\s*color = TextPrimary,\s*fontSize = 12\.sp,\s*fontWeight = FontWeight\.SemiBold,",
    """Text(
                    text = activity.song.title,
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,""",
    text
)

# And in EqualizerBars inside CompactRightContent, the user wants "the wave in pill is on edge"
# Let's check how CompactRightContent is called. Wait, I should just adjust padding in CompactIslandPill.

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "w") as f:
    f.write(text)
