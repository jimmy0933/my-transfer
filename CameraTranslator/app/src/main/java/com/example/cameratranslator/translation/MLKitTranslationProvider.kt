package com.example.cameratranslator.translation

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.common.model.RemoteModelManager
import kotlinx.coroutines.tasks.await

class MLKitTranslationProvider : TranslationProvider {

    private val translators = mutableMapOf<String, Translator>()

    override suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        val sourceLangCode = mapLanguageCode(sourceLanguage)
        val targetLangCode = mapLanguageCode(targetLanguage)

        val key = "${sourceLangCode}_$targetLangCode"
        val translator = translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangCode)
                .setTargetLanguage(targetLangCode)
                .build()
            Translation.getClient(options)
        }

        return try {
            translator.translate(text).await()
        } catch (e: Exception) {
            text // Fallback to original text on failure
        }
    }

    override suspend fun downloadModelIfNeeded(languageCode: String) {
        val langCode = mapLanguageCode(languageCode)
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH) // dummy pair to just trigger download
            .setTargetLanguage(langCode)
            .build()
        val client = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()
        client.downloadModelIfNeeded(conditions).await()
    }

    override suspend fun isModelDownloaded(languageCode: String): Boolean {
        val langCode = mapLanguageCode(languageCode)
        val model = TranslateRemoteModel.Builder(langCode).build()
        val modelManager = RemoteModelManager.getInstance()
        return try {
            val isDownloaded = modelManager.isModelDownloaded(model).await()
            isDownloaded
        } catch (e: Exception) {
            false
        }
    }

    private fun mapLanguageCode(lang: String): String {
        return when (lang.lowercase()) {
            "en", "english" -> TranslateLanguage.ENGLISH
            "ja", "japanese" -> TranslateLanguage.JAPANESE
            "th", "thai" -> TranslateLanguage.THAI
            "zh", "zh-tw", "chinese" -> TranslateLanguage.CHINESE
            else -> TranslateLanguage.ENGLISH
        }
    }
}
