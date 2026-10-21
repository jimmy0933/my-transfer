import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ui/MainScreen.kt', 'r') as f:
    text = f.read()

text = text.replace('fun MainScreen(viewModel: MainViewModel) {', 'fun MainScreen(viewModel: MainViewModel, onNavigateToPicker: () -> Unit, onNavigateToSettings: () -> Unit) {')

text = text.replace('// Settings Icon', 'Button(onClick = onNavigateToSettings) { Text("設定") }')
text = text.replace('Button(onClick = { \n                isPaused = !isPaused', 'Button(onClick = onNavigateToPicker) { Text("相簿") }\n            Button(onClick = { \n                isPaused = !isPaused')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ui/MainScreen.kt', 'w') as f:
    f.write(text)


with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/MainActivity.kt', 'r') as f:
    text = f.read()

text = text.replace('MainScreen(viewModel)', 'com.example.cameratranslator.ui.MainAppNavigation(viewModel)')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/MainActivity.kt', 'w') as f:
    f.write(text)
