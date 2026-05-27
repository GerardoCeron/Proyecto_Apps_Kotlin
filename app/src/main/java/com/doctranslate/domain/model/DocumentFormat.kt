package com.doctranslate.domain.model

enum class DocumentFormat(val displayName: String, val extension: String) {
    TEXT("Texto plano (.txt)", "txt"),
    HTML("Página Web (.html)", "html"),
    MARKDOWN("Markdown (.md)", "md"),
    JSON("JSON (.json)", "json"),
    PDF("PDF (.pdf)", "pdf")
}
