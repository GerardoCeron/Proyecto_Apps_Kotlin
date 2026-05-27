package com.doctranslate.presentation.viewmodel

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctranslate.data.converter.ConverterFactory
import com.doctranslate.data.remote.Downloader
import com.doctranslate.domain.model.DocumentFormat
import com.doctranslate.domain.model.Language
import com.doctranslate.domain.model.supportedLanguages
import com.doctranslate.domain.usecase.GenerateAudioUseCase
import com.doctranslate.domain.usecase.TranslateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val translateUseCase: TranslateUseCase,
    private val generateAudioUseCase: GenerateAudioUseCase,
    private val downloader: Downloader
) : ViewModel() {

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _downloadUrl = MutableStateFlow<String?>(null)
    val downloadUrl: StateFlow<String?> = _downloadUrl.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(supportedLanguages[0])
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _selectedFormat = MutableStateFlow(DocumentFormat.TEXT)
    val selectedFormat: StateFlow<DocumentFormat> = _selectedFormat.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _isGeneratingAudio = MutableStateFlow(false)
    val isGeneratingAudio: StateFlow<Boolean> = _isGeneratingAudio.asStateFlow()

    private var _originalFileName = "translated_document"
    private var _mediaPlayer: MediaPlayer? = null

    fun onLanguageSelected(language: Language) {
        _selectedLanguage.value = language
    }

    fun onFormatSelected(format: DocumentFormat) {
        _selectedFormat.value = format
    }

    fun translateFile(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isTranslating.value = true
            _translatedText.value = "Extrayendo texto del documento..."
            _downloadUrl.value = null
            try {
                val contentResolver = context.contentResolver
                var fileName = "document"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
                
                _originalFileName = "translated_$fileName"
                if (!_originalFileName.endsWith(".txt")) {
                    _originalFileName += ".txt"
                }
                
                val extension = fileName.substringAfterLast(".", "").lowercase()
                val converter = ConverterFactory.createFromExtension(extension)
                val extractedText = converter.extractText(context, uri)

                if (extractedText.isNotBlank() && !extractedText.startsWith("Error")) {
                    _translatedText.value = "Traduciendo..."
                    val result = translateUseCase(
                        text = extractedText,
                        source = "auto",
                        target = _selectedLanguage.value.code
                    )
                    _translatedText.value = result
                } else {
                    _translatedText.value = extractedText.ifBlank { "No se pudo extraer texto del archivo" }
                }
            } catch (e: Exception) {
                _translatedText.value = "Error: ${e.message}"
            } finally {
                _isTranslating.value = false
            }
        }
    }

    fun generateAndPlayAudio(context: Context) {
        val text = _translatedText.value
        if (text.isBlank() || text.startsWith("Error") || text.startsWith("Extrayendo") || text.startsWith("Traduciendo")) return

        viewModelScope.launch {
            _isGeneratingAudio.value = true
            try {
                val audioData = generateAudioUseCase(text)
                if (audioData.isNotEmpty()) {
                    playAudio(context, audioData)
                    saveAudioToFile(context, audioData, "audio_${System.currentTimeMillis()}.mp3")
                } else {
                    _translatedText.value = "Error al generar audio (verificar API key)"
                }
            } catch (e: Exception) {
                _translatedText.value = "Error de audio: ${e.message}"
            } finally {
                _isGeneratingAudio.value = false
            }
        }
    }

    private fun playAudio(context: Context, audioData: ByteArray) {
        try {
            val tempFile = File.createTempFile("temp_audio", "mp3", context.cacheDir)
            tempFile.deleteOnExit()
            FileOutputStream(tempFile).use { it.write(audioData) }

            _mediaPlayer?.release()
            _mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            _translatedText.value = "Error al reproducir: ${e.message}"
        }
    }

    private fun saveAudioToFile(context: Context, audioData: ByteArray, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { it.write(audioData) }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(audioData) }
            }
        } catch (e: Exception) {
            // Silently fail or log for save
        }
    }

    fun downloadTranslatedFile(context: Context) {
        val url = _downloadUrl.value
        if (!url.isNullOrBlank()) {
            downloader.downloadFile(url, _originalFileName)
        } else if (_translatedText.value.isNotBlank() && !_translatedText.value.startsWith("Error")) {
            saveTextToFile(context, _translatedText.value, _originalFileName)
        }
    }

    private fun saveTextToFile(context: Context, text: String, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(text.toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(text.toByteArray())
                }
            }
        } catch (e: Exception) {
            _translatedText.value = "Error al guardar archivo: ${e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        _mediaPlayer?.release()
        _mediaPlayer = null
    }
}
