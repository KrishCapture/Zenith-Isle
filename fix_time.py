with open("app/src/main/java/com/example/ui/components/PhoneSimulatorCanvas.kt", "r") as f:
    text = f.read()

new_func = """@Composable
private fun rememberTime(): String {
    val time = remember {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.format(Date())
    }
    return time
}"""

import re
text = re.sub(r"@Composable\s*private fun rememberTime\(\): String \{.*?(?=\Z|\n@Composable|\n\n\n)", new_func + "\n", text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/components/PhoneSimulatorCanvas.kt", "w") as f:
    f.write(text)
