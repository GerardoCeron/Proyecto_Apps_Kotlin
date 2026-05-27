package com.doctranslate.data.converter

import android.content.Context
import android.net.Uri

class PlainTextConverter : DocumentConverter {
    override suspend fun extractText(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            "Error al leer el archivo: ${e.message}"
        }
    }
}
