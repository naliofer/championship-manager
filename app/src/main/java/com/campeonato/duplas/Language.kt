package com.campeonato.duplas

import java.util.Locale

object Language {
    val tags = listOf("pt-BR", "en-US", "es-ES")
    val names = listOf("Português (Brasil)", "English (United States)", "Español (España)")
    fun detect(tag: String): String = when (Locale.forLanguageTag(tag).language) {
        "en" -> "en-US"
        "es" -> "es-ES"
        else -> "pt-BR"
    }
}
