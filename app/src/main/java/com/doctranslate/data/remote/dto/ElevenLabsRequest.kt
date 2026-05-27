package com.doctranslate.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ElevenLabsRequest(
    val text: String,
    @SerializedName("model_id")
    val modelId: String = "eleven_multilingual_v2"
)
