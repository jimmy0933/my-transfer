import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'r') as f:
    text = f.read()

text = text.replace('@HiltViewModel\n@HiltViewModel', '@HiltViewModel')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'w') as f:
    f.write(text)
