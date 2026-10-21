package com.example.cameratranslator.core

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.camera.core.CameraControl
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cameratranslator.core.models.TextElement
import com.example.cameratranslator.history.HistoryDao
import com.example.cameratranslator.history.TranslationHistory
import com.example.cameratranslator.ocr.MLKitOcrProvider
import com.example.cameratranslator.ocr.OcrProvider
import com.example.cameratranslator.translation.MLKitTranslationProvider
import com.example.cameratranslator.translation.TranslationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val historyDao: HistoryDao
) : AndroidViewModel(application) {

    private val ocrProvider: OcrProvider = MLKitOcrProvider()
    private val translationProvider: TranslationProvider = MLKitTranslationProvider()

    private val _textElements = MutableStateFlow<List<TextElement>>(emptyList())
    val textElements: StateFlow<List<TextElement>> = _textElements.asStateFlow()

    private val translationCache = mutableMapOf<String, String>()

    var isPaused = false
    var showOriginal = false

    var cameraControl: CameraControl? = null
    var isFlashlightOn = false
    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        cameraControl?.enableTorch(isFlashlightOn)
    }

    init {
        viewModelScope.launch {
            try {
                translationProvider.downloadModelIfNeeded("zh-tw")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun processImage(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val recognizedTexts = ocrProvider.recognizeText(imageProxy, rotation)
                val translatedElements = recognizedTexts.map { element ->
                    val original = element.originalText
                    val cached = translationCache[original]
                    if (cached != null) {
                        element.copy(translatedText = cached)
                    } else {
                        val translated = translationProvider.translate(original, element.sourceLanguage, "zh-tw")
                        translationCache[original] = translated
                        element.copy(translatedText = translated)
                    }
                }
                _textElements.value = translatedElements
            } finally {
                imageProxy.close()
            }
        }
    }

    fun processBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val recognizedTexts = ocrProvider.recognizeTextFromBitmap(bitmap)
            val translatedElements = recognizedTexts.map { element ->
                val original = element.originalText
                val cached = translationCache[original]
                if (cached != null) {
                    element.copy(translatedText = cached)
                } else {
                    val translated = translationProvider.translate(original, element.sourceLanguage, "zh-tw")
                    translationCache[original] = translated
                    element.copy(translatedText = translated)
                }
            }
            _textElements.value = translatedElements
        }
    }

    fun saveToHistory(element: TextElement) {
        viewModelScope.launch {
            historyDao.insertHistory(
                TranslationHistory(
                    sourceLanguage = element.sourceLanguage,
                    originalText = element.originalText,
                    translatedText = element.translatedText ?: ""
                )
            )
        }
    }
}
