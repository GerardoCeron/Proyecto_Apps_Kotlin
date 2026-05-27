package com.doctranslate.data.converter

import android.content.Context
import android.net.Uri

interface DocumentConverter {
    suspend fun extractText(context: Context, uri: Uri): String
}
