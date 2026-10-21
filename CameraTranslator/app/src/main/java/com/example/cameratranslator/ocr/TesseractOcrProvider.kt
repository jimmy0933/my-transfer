package com.example.cameratranslator.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.example.cameratranslator.core.models.TextElement
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class TesseractOcrProvider(private val context: Context) : OcrProvider {
    private val tess: TessBaseAPI = TessBaseAPI()
    private var isInitialized = false
    private val dataPath = File(context.filesDir, "tesseract")
    private val languageIdentifier = LanguageIdentification.getClient()

    init {
        initTesseract()
    }

    private fun initTesseract() {
        try {
            val tessdata = File(dataPath, "tessdata")
            if (!tessdata.exists()) {
                tessdata.mkdirs()
            }
            tess.init(dataPath.absolutePath, "eng") // eng or tha depending on available dataset
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun recognizeText(image: ImageProxy, rotationDegrees: Int): List<TextElement> = withContext(Dispatchers.Default) {
        val bitmap = imageProxyToBitmap(image, rotationDegrees)
        recognizeTextFromBitmap(bitmap)
    }

    override suspend fun recognizeTextFromBitmap(bitmap: Bitmap): List<TextElement> = withContext(Dispatchers.Default) {
        if (!isInitialized) return@withContext emptyList()

        tess.setImage(bitmap)
        val iterator = tess.resultIterator
        val blocks = mutableListOf<TextElement>()

        if (iterator != null) {
            iterator.begin()
            do {
                val text = iterator.getUTF8Text(PageIteratorLevel.RIL_BLOCK)
                if (!text.isNullOrBlank()) {
                    val rect = iterator.getBoundingBox(PageIteratorLevel.RIL_BLOCK)

                    val langCode = try {
                        val lang = languageIdentifier.identifyLanguage(text).await()
                        if (lang == "und") "th" else lang // Default to Thai if using Tesseract usually
                    } catch (e: Exception) {
                        "th"
                    }

                    blocks.add(TextElement(
                        id = UUID.randomUUID().toString(),
                        originalText = text,
                        boundingBox = android.graphics.Rect(rect[0], rect[1], rect[2], rect[3]),
                        sourceLanguage = langCode
                    ))
                }
            } while (iterator.next(PageIteratorLevel.RIL_BLOCK))
            iterator.delete()
        } else {
            val text = tess.utF8Text
            if (!text.isNullOrBlank()) {
                blocks.add(TextElement(
                    id = UUID.randomUUID().toString(),
                    originalText = text,
                    boundingBox = android.graphics.Rect(0, 0, bitmap.width, bitmap.height),
                    sourceLanguage = "th"
                ))
            }
        }

        tess.clear()
        blocks
    }

    private fun imageProxyToBitmap(image: ImageProxy, rotationDegrees: Int): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }
}
