package com.example.cameratranslator.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.example.cameratranslator.core.models.TextElement
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MLKitOcrProvider : OcrProvider {
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val languageIdentifier = LanguageIdentification.getClient()

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

    private suspend fun processImage(image: InputImage): List<TextElement> = coroutineScope {
        try {
            val jpDeferred = async {
                try { japaneseRecognizer.process(image).await() } catch(e: Exception) { null }
            }
            val latinDeferred = async {
                try { latinRecognizer.process(image).await() } catch(e: Exception) { null }
            }

            val jpResult = jpDeferred.await()
            val latinResult = latinDeferred.await()

            val allBlocks = mutableListOf<com.google.mlkit.vision.text.Text.TextBlock>()
            if (jpResult != null) allBlocks.addAll(jpResult.textBlocks)
            if (latinResult != null) {
                val jpRects = jpResult?.textBlocks?.mapNotNull { it.boundingBox } ?: emptyList()
                val uniqueLatin = latinResult.textBlocks.filter { latinBlock ->
                    val lRect = latinBlock.boundingBox ?: return@filter false
                    jpRects.none { jpRect ->
                        jpRect.contains(lRect.centerX(), lRect.centerY()) ||
                        lRect.contains(jpRect.centerX(), jpRect.centerY())
                    }
                }
                allBlocks.addAll(uniqueLatin)
            }

            allBlocks.map { block ->
                val langCode = try {
                    val lang = languageIdentifier.identifyLanguage(block.text).await()
                    if (lang == "und") "auto" else lang
                } catch (e: Exception) {
                    "auto"
                }

                TextElement(
                    id = UUID.randomUUID().toString(),
                    originalText = block.text,
                    boundingBox = block.boundingBox,
                    sourceLanguage = langCode
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
