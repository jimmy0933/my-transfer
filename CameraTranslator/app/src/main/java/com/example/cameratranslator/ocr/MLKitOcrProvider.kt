package com.example.cameratranslator.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.example.cameratranslator.core.models.TextElement
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MLKitOcrProvider : OcrProvider {
    // Currently initializing only Latin and Japanese as per defaults.
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun recognizeText(image: ImageProxy, rotationDegrees: Int): List<TextElement> {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            return processImage(inputImage)
        }
        return emptyList()
    }

    override suspend fun recognizeTextFromBitmap(bitmap: Bitmap): List<TextElement> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return processImage(inputImage)
    }

    private suspend fun processImage(image: InputImage): List<TextElement> {
        // Attempt Japanese recognition first; if results are lacking, could fallback or run concurrently.
        // For simplicity, we can run both and merge, but running Japanese usually includes Latin alphabet reasonably well.
        return try {
            val result = japaneseRecognizer.process(image).await()
            result.textBlocks.map { block ->
                TextElement(
                    id = UUID.randomUUID().toString(),
                    originalText = block.text,
                    boundingBox = block.boundingBox,
                    sourceLanguage = "ja/en" // Defaulting label; Language detection can be added
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
