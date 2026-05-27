package com.doctranslate.domain.repository

interface AudioRepository {
    suspend fun generateAudio(text: String): ByteArray
}