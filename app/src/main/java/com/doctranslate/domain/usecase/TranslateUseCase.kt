package com.doctranslate.domain.usecase

import com.doctranslate.domain.repository.TranslationRepository
import javax.inject.Inject

class TranslateUseCase @Inject constructor(
    private val repository: TranslationRepository
) {

    suspend operator fun invoke(
        text: String,
        source: String,
        target: String
    ): String {
        return repository.translateText(text, source, target)
    }

    suspend fun translateFile(
        fileData: ByteArray,
        fileName: String,
        source: String,
        target: String
    ): String {
        return repository.translateFile(fileData, fileName, source, target)
    }
}
