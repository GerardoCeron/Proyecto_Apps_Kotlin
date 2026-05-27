package com.doctranslate.data.remote.api

import com.doctranslate.data.remote.dto.TranslationFileResponse
import com.doctranslate.data.remote.dto.TranslationRequest
import com.doctranslate.data.remote.dto.TranslationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface TranslationApi {

    @POST
    suspend fun translate(
        @Url url: String,
        @Body request: TranslationRequest
    ): TranslationResponse

    @Multipart
    @POST
    suspend fun translateFile(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part("source") source: RequestBody,
        @Part("target") target: RequestBody
    ): TranslationFileResponse
}
