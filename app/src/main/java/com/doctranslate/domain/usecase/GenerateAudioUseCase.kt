package com.doctranslate.domain.usecase

import com.doctranslate.domain.repository.AudioRepository
import javax.inject.Inject

class GenerateAudioUseCase @Inject constructor(
    private val repository: AudioRepository
) {
    suspend operator fun invoke(text: String): ByteArray {
        return repository.generateAudio(text)
    }
}
