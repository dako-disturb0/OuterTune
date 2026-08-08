package com.dd3boh.outertune.utils

import android.content.Context

data class TranslatorLanguage(
    val code: String,
    val name: String,
)

typealias TranslatorLang = TranslatorLanguage

object TranslatorLanguages {
    fun load(context: Context): List<TranslatorLanguage> {
        return listOf(
            TranslatorLanguage("en", "English"),
            TranslatorLanguage("id", "Indonesian"),
            TranslatorLanguage("ja", "Japanese"),
            TranslatorLanguage("ko", "Korean"),
            TranslatorLanguage("zh", "Chinese"),
            TranslatorLanguage("es", "Spanish"),
            TranslatorLanguage("fr", "French"),
            TranslatorLanguage("de", "German"),
            TranslatorLanguage("ru", "Russian"),
            TranslatorLanguage("ar", "Arabic"),
        )
    }
}
