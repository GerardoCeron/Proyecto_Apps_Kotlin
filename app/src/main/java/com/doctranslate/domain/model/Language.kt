package com.doctranslate.domain.model

data class Language(
    val name: String,
    val code: String
)

val supportedLanguages = listOf(
    Language("Español", "es"),
    Language("Inglés", "en"),
    Language("Francés", "fr"),
    Language("Alemán", "de"),
    Language("Italiano", "it"),
    Language("Portugués", "pt"),
    Language("Ruso", "ru"),
    Language("Chino", "zh"),
    Language("Japonés", "ja"),
    Language("Coreano", "ko")
)
