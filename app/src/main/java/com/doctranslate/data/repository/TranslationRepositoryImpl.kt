package com.doctranslate.data.repository

import android.util.Log
import com.doctranslate.data.remote.OfflineTranslationManager
import com.doctranslate.data.remote.api.MyMemoryApi
import com.doctranslate.data.remote.api.TranslationApi
import com.doctranslate.data.remote.dto.TranslationRequest
import com.doctranslate.domain.repository.TranslationRepository
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(
    private val api: TranslationApi,
    private val myMemoryApi: MyMemoryApi,
    private val offlineManager: OfflineTranslationManager
) : TranslationRepository {

    private val mirrors = listOf(
        "https://translate.argosopentech.com/",
        "https://libretranslate.de/",
        "https://translate.fedilab.app/",
        "https://translate.terraprint.co/",
        "https://lt.vern.cc/",
        "https://translate.cutie.dating/",
        "https://trans.zillyhuhn.com/",
        "https://translate.mentality.rip/",
        "https://lt.psf.lt/",
        "https://translate.astian.org/",
        "https://libretranslate.pussthecat.org/",
        "https://translate.garudalinux.org/"
    )

    override suspend fun translateText(
        text: String,
        source: String,
        target: String
    ): String {
        var lastException: Exception? = null
        
        // 1. Intentar con mirrors de LibreTranslate
        for (mirror in mirrors) {
            try {
                return api.translate(
                    url = "${mirror}translate",
                    request = TranslationRequest(
                        q = text,
                        source = source,
                        target = target
                    )
                ).translatedText
            } catch (e: Exception) {
                lastException = e
                Log.w("TranslationRepository", "LibreTranslate mirror failed: $mirror")
                delay(500)
                continue
            }
        }
        
        // 2. Intentar con MyMemory API (Cloud Fallback)
        try {
            Log.i("TranslationRepository", "Attempting MyMemory fallback")
            return myMemoryApi.translate(
                text = text,
                langPair = "$source|$target"
            ).responseData.translatedText
        } catch (e: Exception) {
            Log.e("TranslationRepository", "MyMemory fallback failed: ${e.message}")
        }

        // 3. ULTIMO NIVEL: Google ML Kit Offline Translation
        return try {
            Log.i("TranslationRepository", "Attempting ULTIMATE Offline fallback")
            // Mapeo básico de 'auto' a 'en' si es necesario para ML Kit, 
            // aunque usualmente source ya viene detectado o se usa 'en' por defecto.
            val finalSource = if (source == "auto") "en" else source
            offlineManager.translate(text, finalSource, target)
        } catch (e: Exception) {
            Log.e("TranslationRepository", "Offline fallback failed: ${e.message}")
            throw lastException ?: e
        }
    }

    override suspend fun translateFile(
        fileData: ByteArray,
        fileName: String,
        source: String,
        target: String
    ): String {
        var lastException: Exception? = null

        val filePart = MultipartBody.Part.createFormData(
            "file",
            fileName,
            fileData.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        )
        
        val sourceBody = source.toRequestBody("text/plain".toMediaTypeOrNull())
        val targetBody = target.toRequestBody("text/plain".toMediaTypeOrNull())

        for (mirror in mirrors) {
            try {
                return api.translateFile(
                    url = "${mirror}translate_file",
                    file = filePart,
                    source = sourceBody,
                    target = targetBody
                ).translatedFileUrl
            } catch (e: Exception) {
                lastException = e
                Log.w("TranslationRepository", "File mirror failed: $mirror")
                delay(500)
                continue
            }
        }
        
        throw lastException ?: Exception("Error en la traducción de archivo")
    }
}
