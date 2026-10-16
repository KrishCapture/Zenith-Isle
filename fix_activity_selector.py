import re

with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "r") as f:
    text = f.read()

# Remove songs parameter
text = re.sub(r"songs: List<Song>,?\s*", "", text)
# Remove the row/UI for switching songs
text = re.sub(r"        // Quick Actions for Current Activity.*?(\n\s*)*if \(activeActivity is LiveActivityState\.Music\) \{.*?\}\n", "", text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("songs = songs,\n", "")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

