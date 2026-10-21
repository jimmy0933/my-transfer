import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ocr/MLKitOcrProvider.kt', 'r') as f:
    text = f.read()

text = text.replace('override suspend fun recognizeText(image: ImageProxy, rotationDegrees: Int): List<TextElement> {', '@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)\n    override suspend fun recognizeText(image: ImageProxy, rotationDegrees: Int): List<TextElement> {')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ocr/MLKitOcrProvider.kt', 'w') as f:
    f.write(text)
