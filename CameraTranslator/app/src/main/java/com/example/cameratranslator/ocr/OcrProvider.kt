package com.example.cameratranslator.ocr

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import com.example.cameratranslator.core.models.TextElement

interface OcrProvider {
    suspend fun recognizeText(image: ImageProxy, rotationDegrees: Int): List<TextElement>
    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): List<TextElement>
}
