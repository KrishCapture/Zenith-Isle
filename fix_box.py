with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "Box(" in line and "size(52.dp)" in lines[i+2]:
        # we found the play pause box
        pass
