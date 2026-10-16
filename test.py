with open("app/src/main/java/com/example/ui/components/ActivitySelectorBar.kt", "r") as f:
    text = f.read()

lines = text.split('\n')
open_braces = 0
for i, line in enumerate(lines):
    open_braces += line.count('{')
    open_braces -= line.count('}')
    print(f"Line {i+1}: {line.strip()} | Braces: {open_braces}")
    if open_braces == 0 and "}" in line:
        pass

