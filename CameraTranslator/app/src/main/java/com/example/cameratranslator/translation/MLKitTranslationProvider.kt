package com.example.cameratranslator.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class MLKitTranslationProvider : TranslationProvider {
    private val translators = mutableMapOf<String, Translator>()
    private val downloadedPairs = mutableSetOf<String>()
    private val mutex = Mutex()

    override suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String? {
        if (text.isBlank()) return null
        val source = code(sourceLanguage) ?: return null
        val target = code(targetLanguage) ?: return null
        if (source == target) return null

        return try {
            // ML Kit needs the source AND target language model. Download the actual pair
            // before translate(), rather than only downloading English -> Chinese at startup.
            val translator = mutex.withLock {
                val key = source + ":" + target
                val client = translators.getOrPut(key) {
                    Translation.getClient(TranslatorOptions.Builder()
                        .setSourceLanguage(source).setTargetLanguage(target).build())
                }
                if (!downloadedPairs.contains(key)) {
                    client.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
                    downloadedPairs.add(key)
                }
                client
            }
            translator.translate(text).await().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun downloadModelIfNeeded(languageCode: String) {
        // Download the required pair on demand in translate(), not a dummy language pair.
    }

    private fun code(lang: String): String? = when (lang.lowercase()) {
        "en", "english" -> TranslateLanguage.ENGLISH
        "ja", "japanese" -> TranslateLanguage.JAPANESE
        "th", "thai" -> TranslateLanguage.THAI
        "zh", "zh-tw", "chinese" -> TranslateLanguage.CHINESE
        else -> null
    }
}
