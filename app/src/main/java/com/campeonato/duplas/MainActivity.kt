package com.campeonato.duplas

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.campeonato.duplas.ui.screens.ModalityApp
import com.campeonato.duplas.ui.theme.CampeonatoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("championship", Activity.MODE_PRIVATE)
        setContent {
            CampeonatoTheme {
                ModalityApp(prefs)
            }
        }
    }
}
