package com.doctranslate.data.remote.api

import com.doctranslate.data.remote.dto.ElevenLabsRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ElevenLabsApi {

    @Headers(
        "xi-api-key: ",
        "Content-Type: application/json"
    )
    @POST("v1/text-to-speech/{voice_id}")
    suspend fun generateAudio(
        @Path("voice_id") voiceId: String,
        @Body request: ElevenLabsRequest
    ): Response<ResponseBody>
}