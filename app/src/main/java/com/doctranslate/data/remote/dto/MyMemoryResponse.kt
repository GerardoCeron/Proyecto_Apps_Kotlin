package com.doctranslate.data.remote.dto

data class MyMemoryResponse(
    val responseData: MyMemoryData,
    val responseStatus: Int
)

data class MyMemoryData(
    val translatedText: String
)
