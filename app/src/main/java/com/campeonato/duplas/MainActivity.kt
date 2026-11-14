package com.campeonato.duplas

import androidx.activity.enableEdgeToEdge
import android.app.Activity
import android.os.Bundle
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.campeonato.duplas.ui.screens.ModalityApp
import com.campeonato.duplas.ui.theme.CampeonatoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val prefs = getSharedPreferences("championship", Activity.MODE_PRIVATE)
        setContent {
            var language by remember { mutableStateOf(prefs.getString("language", null)?.takeIf { it in Language.tags }
                ?: Language.detect(resources.configuration.locales[0].toLanguageTag())) }
            val config = Configuration(LocalConfiguration.current).apply { setLocale(Locale.forLanguageTag(language)) }
            val localized = remember(language, config) { createConfigurationContext(config) }
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides config) {
                CampeonatoTheme {
                    ModalityApp(prefs, language) { tag ->
                        language = tag
                        prefs.edit().putString("language", tag).apply()
                    }
                }
            }
        }
    }
}
