package com.doctranslate.data.remote.dto

data class TranslationRequest(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text"
)