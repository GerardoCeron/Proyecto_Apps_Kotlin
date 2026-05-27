package com.doctranslate.data.converter

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

class PdfDocumentConverter : DocumentConverter {

    override suspend fun extractText(context: Context, uri: Uri): String {
        return try {
            PDFBoxResourceLoader.init(context)
            val inputStream = context.contentResolver.openInputStream(uri)
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            // sortByPosition = false a menudo respeta mejor el flujo natural de columnas
            stripper.sortByPosition = false 
            val rawText = stripper.getText(document)
            document.close()
            
            // Limpieza básica: quitar espacios múltiples y normalizar saltos de línea
            rawText.replace(Regex(" {2,}"), " ")
                  .replace(Regex("(\\r?\\n){3,}"), "\n\n")
                  .trim()
        } catch (e: Exception) {
            "Error al extraer texto del PDF: ${e.message}"
        }
    }
}
