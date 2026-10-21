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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val historyDao: HistoryDao,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val ocrProvider: OcrProvider = MLKitOcrProvider()
    private val translationProvider: TranslationProvider = MLKitTranslationProvider()

    private val _textElements = MutableStateFlow<List<TextElement>>(emptyList())
    val textElements: StateFlow<List<TextElement>> = _textElements.asStateFlow()

    private val translationCache = mutableMapOf<String, String>()

    var isPaused = false
    var showOriginal = false

    var imageWidth = 1
    var imageHeight = 1

    var cameraControl: CameraControl? = null
    var isFlashlightOn = false

    val transparency = settingsRepository.transparencyFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)
    private val _tempTransparency = MutableStateFlow<Float?>(null)

    val currentTransparency: StateFlow<Float> = combine(transparency, _tempTransparency) { saved, temp ->
        temp ?: saved
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)

    private val _isChineseModelDownloaded = MutableStateFlow(false)
    val isChineseModelDownloaded = _isChineseModelDownloaded.asStateFlow()

    // Tracking and debouncing state
    private val activeElements = mutableMapOf<String, TrackedElement>()

    data class TrackedElement(
        val element: TextElement,
        var lastSeen: Long
    )

    fun setTempTransparency(t: Float) {
        _tempTransparency.value = t
    }

    fun revertTempTransparency() {
        _tempTransparency.value = null
    }

    suspend fun saveTransparency(t: Float) {
        settingsRepository.saveTransparency(t)
        _tempTransparency.value = null
    }

    fun checkChineseModelStatus() {
        viewModelScope.launch {
            _isChineseModelDownloaded.value = translationProvider.isModelDownloaded("zh-tw")
        }
    }

    fun downloadChineseModel() {
        viewModelScope.launch {
            translationProvider.downloadModelIfNeeded("zh-tw")
            checkChineseModelStatus()
        }
    }

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        cameraControl?.enableTorch(isFlashlightOn)
    }

    init {
        viewModelScope.launch {
            try {
                checkChineseModelStatus()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun updateActiveElements(newElements: List<TextElement>): List<TextElement> {
        val currentTime = System.currentTimeMillis()
        val retentionTimeMs = 1500L

        val matchedIds = mutableSetOf<String>()

        val updatedElements = newElements.map { newEl ->
            var bestMatchId: String? = null
            var highestIoU = 0.0

            val newBox = newEl.boundingBox
            if (newBox != null) {
                for ((id, tracked) in activeElements) {
                    val oldBox = tracked.element.boundingBox
                    if (oldBox != null) {
                        val intersect = android.graphics.Rect(oldBox)
                        if (intersect.intersect(newBox)) {
                            val intersectArea = intersect.width() * intersect.height()
                            val unionArea = (oldBox.width() * oldBox.height()) + (newBox.width() * newBox.height()) - intersectArea
                            val iou = intersectArea.toDouble() / unionArea.toDouble()

                            if (iou > 0.3 && iou > highestIoU) {
                                highestIoU = iou
                                bestMatchId = id
                            }
                        }
                    }
                }
            }

            val idToUse = bestMatchId ?: newEl.id
            matchedIds.add(idToUse)

            val elementToStore = if (bestMatchId != null) {
                val existing = activeElements[bestMatchId]!!.element
                newEl.copy(id = idToUse, translatedText = existing.translatedText ?: newEl.translatedText)
            } else {
                newEl
            }

            activeElements[idToUse] = TrackedElement(elementToStore, currentTime)
            elementToStore
        }

        activeElements.entries.removeIf { (id, tracked) ->
            if (id !in matchedIds) {
                if (currentTime - tracked.lastSeen > retentionTimeMs) {
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        return activeElements.values.map { it.element }
    }

    fun processImage(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation == 90 || rotation == 270) {
            imageWidth = imageProxy.height
            imageHeight = imageProxy.width
        } else {
            imageWidth = imageProxy.width
            imageHeight = imageProxy.height
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val recognizedTexts = ocrProvider.recognizeText(imageProxy, rotation)
                val stableElements = updateActiveElements(recognizedTexts)

                val translatedElements = stableElements.map { element ->
                    val original = element.originalText
                    val cached = translationCache[original]
                    if (cached != null) {
                        element.copy(translatedText = cached)
                    } else if (element.translatedText != null) {
                        element
                    } else {
                        val translated = translationProvider.translate(original, element.sourceLanguage, "zh-tw")
                        translationCache[original] = translated
                        val el = element.copy(translatedText = translated)
                        activeElements[element.id]?.let {
                            activeElements[element.id] = it.copy(element = el)
                        }
                        el
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
