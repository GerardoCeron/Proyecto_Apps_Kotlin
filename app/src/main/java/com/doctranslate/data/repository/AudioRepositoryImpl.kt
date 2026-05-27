package com.doctranslate.data.repository

import android.util.Log
import com.doctranslate.data.remote.api.ElevenLabsApi
import com.doctranslate.data.remote.dto.ElevenLabsRequest
import com.doctranslate.domain.repository.AudioRepository
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val api: ElevenLabsApi
) : AudioRepository {

    override suspend fun generateAudio(text: String): ByteArray {
        return try {
            val response = api.generateAudio(
                voiceId = "EXAVITQu4vr4xnSDxMaL", // Rachel
                request = ElevenLabsRequest(text = text)
            )

            if (response.isSuccessful) {
                response.body()?.bytes() ?: byteArrayOf()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AudioRepository", "Error de ElevenLabs: Code ${response.code()} - $errorBody")
                byteArrayOf()
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Excepción al generar audio: ${e.message}", e)
            byteArrayOf()
        }
    }
}
