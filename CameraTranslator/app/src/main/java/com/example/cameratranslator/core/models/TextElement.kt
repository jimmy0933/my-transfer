package com.example.cameratranslator.core.models

import android.graphics.Rect

data class TextElement(
    val id: String,
    val originalText: String,
    val translatedText: String? = null,
    val boundingBox: Rect?,
    val sourceLanguage: String = "auto"
)
