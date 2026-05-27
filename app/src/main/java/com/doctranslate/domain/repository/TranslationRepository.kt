package com.doctranslate.domain.repository

interface TranslationRepository {

    suspend fun translateText(
        text: String,
        source: String,
        target: String
    ): String

    suspend fun translateFile(
        fileData: ByteArray,
        fileName: String,
        source: String,
        target: String
    ): String
}
