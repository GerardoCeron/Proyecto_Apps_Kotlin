package com.doctranslate.data.remote

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineTranslationManager @Inject constructor() {

    suspend fun translate(text: String, source: String, target: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()
            
        val translator = Translation.getClient(options)
        
        return try {
            val conditions = DownloadConditions.Builder()
                .build() // Permitir descarga con cualquier conexión la primera vez

            translator.downloadModelIfNeeded(conditions).await()
            translator.translate(text).await()
        } catch (e: Exception) {
            "Error Offline: ${e.message}"
        } finally {
            translator.close()
        }
    }
}
