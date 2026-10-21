package com.example.cameratranslator.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceLanguage: String,
    val originalText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis()
)
