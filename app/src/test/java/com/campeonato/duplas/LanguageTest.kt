package com.campeonato.duplas

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {
    @Test fun detectionAndFallback() {
        mapOf("pt-PT" to "pt-BR", "en-GB" to "en-US", "es-MX" to "es-ES", "fr-FR" to "pt-BR", "" to "pt-BR").forEach { (input, expected) ->
            assertEquals(expected, Language.detect(input))
        }
    }
}
