package com.doctranslate.data.remote.api

import com.doctranslate.data.remote.dto.MyMemoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): MyMemoryResponse
}
