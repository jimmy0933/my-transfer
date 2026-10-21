import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'r') as f:
    text = f.read()

text = text.replace('import androidx.camera.core.CameraControl\n\n@HiltViewModel', '@HiltViewModel')
text = text.replace('import androidx.camera.core.ImageProxy\n', 'import androidx.camera.core.ImageProxy\nimport androidx.camera.core.CameraControl\n')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'w') as f:
    f.write(text)
