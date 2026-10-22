package com.example.cameratranslator.translation

interface TranslationProvider {
    /** A null result means translation failed; never present the source as a translation. */
    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String?
    suspend fun downloadModelIfNeeded(languageCode: String)
}
