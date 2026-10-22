package com.example.cameratranslator.core

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.CameraControl
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
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
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val translationCache = ConcurrentHashMap<String, String>()
    private val processing = AtomicBoolean(false)
    private var lastSuccessfulRecognition = 0L
    private val preferences = application.getSharedPreferences("translator_settings", 0)
    var overlayOpacity by mutableFloatStateOf(preferences.getFloat("overlay_opacity", 0.88f))
        private set

    fun setOverlayOpacity(value: Float) {
        overlayOpacity = value.coerceIn(0.35f, 1f)
        preferences.edit().putFloat("overlay_opacity", overlayOpacity).apply()
    }

    @Volatile var isPaused = false
    var showOriginal = false
    var cameraControl: CameraControl? = null
    var isFlashlightOn = false

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        cameraControl?.enableTorch(isFlashlightOn)
    }

    fun processImage(imageProxy: ImageProxy) {
        if (isPaused || !processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val detected = ocrProvider.recognizeText(imageProxy, imageProxy.imageInfo.rotationDegrees)
                translateAndPublish(detected)
            } catch (_: Exception) {
                _statusMessage.value = "辨識失敗，請重新對焦"
            } finally {
                imageProxy.close()
                processing.set(false)
            }
        }
    }

    private suspend fun translateAndPublish(detected: List<TextElement>) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (detected.isEmpty()) {
            if (now - lastSuccessfulRecognition > 1800L) _textElements.value = emptyList()
            _statusMessage.value = "請對準清楚的英文或日文文字"
            return
        }
        lastSuccessfulRecognition = now
        _statusMessage.value = "正在辨識及準備離線翻譯模型…"
        val current = _textElements.value
        val translated = detected.map { element ->
            val original = element.originalText
            val key = element.sourceLanguage + ":" + original
            val old = current.firstOrNull {
                it.originalText == original && it.sourceLanguage == element.sourceLanguage
            }?.translatedText
            val value = translationCache[key] ?: old ?: translationProvider.translate(
                original, element.sourceLanguage, "zh"
            )
            if (!value.isNullOrBlank()) translationCache[key] = value
            element.copy(translatedText = value)
        }
        if (translated.any { it.translatedText != null }) {
            _textElements.value = translated
            _statusMessage.value = null
        } else {
            // Never render a tiny black box containing invisible/empty "translated" text.
            _statusMessage.value = "尚未完成翻譯：請連線下載語言模型後再試"
        }
    }

    fun processBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val detected = ocrProvider.recognizeTextFromBitmap(bitmap)
                translateAndPublish(detected)
            } catch (_: Exception) {
                _statusMessage.value = "圖片辨識失敗"
            }
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
