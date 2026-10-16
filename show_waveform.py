def get_block(text, start_str):
    start = text.find(start_str)
    if start == -1: return -1, -1
    open_braces = 0
    in_block = False
    for i in range(start, len(text)):
        if text[i] == '{':
            open_braces += 1
            in_block = True
        elif text[i] == '}':
            open_braces -= 1
        
        if in_block and open_braces == 0:
            return start, i + 1
    return -1, -1

with open("app/src/main/java/com/example/ui/components/DynamicIslandPill.kt", "r") as f:
    text = f.read()

start, end = get_block(text, "fun RealtimeWaveformView(")
if start != -1:
    print(text[start:end])
