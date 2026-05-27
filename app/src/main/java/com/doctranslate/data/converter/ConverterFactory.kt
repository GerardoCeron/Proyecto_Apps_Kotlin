package com.doctranslate.data.converter

import com.doctranslate.domain.model.DocumentFormat

object ConverterFactory {

    fun create(format: DocumentFormat): DocumentConverter {
        return when (format) {
            DocumentFormat.PDF -> PdfDocumentConverter()
            DocumentFormat.TEXT, 
            DocumentFormat.HTML, 
            DocumentFormat.MARKDOWN, 
            DocumentFormat.JSON -> PlainTextConverter()
        }
    }
    
    fun createFromExtension(extension: String): DocumentConverter {
        return when (extension.lowercase()) {
            "pdf" -> PdfDocumentConverter()
            "jpg", "jpeg", "png", "webp" -> ImageDocumentConverter()
            else -> PlainTextConverter()
        }
    }
}
