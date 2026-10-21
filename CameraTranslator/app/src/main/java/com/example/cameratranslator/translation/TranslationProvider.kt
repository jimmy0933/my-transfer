package com.example.cameratranslator.translation

interface TranslationProvider {
    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String
    suspend fun downloadModelIfNeeded(languageCode: String)
    suspend fun isModelDownloaded(languageCode: String): Boolean
}
