package com.campeonato.duplas.ui.screens

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.campeonato.duplas.R
import com.campeonato.duplas.data.*
import com.campeonato.duplas.ui.components.*
import com.campeonato.duplas.ui.theme.AppSpace

@Composable
fun ModalityApp(prefs: SharedPreferences, language: String, onLanguageChange: (String) -> Unit) {
    var mode by rememberSaveable { mutableStateOf<String?>(null) }
    var settings by remember { mutableStateOf(false) }
    val duplas = remember { ChampionshipRepository(prefs) }
    val cedh = remember { CedhRepository(prefs) }
    // Read only existing snapshots. Never create or overwrite a tournament for the home screen.
    val savedTeams = remember(mode) {
        if (prefs.contains("championship_v2") || prefs.contains("teams") || prefs.contains("names"))
            runCatching { duplas.load() } else null
    }
    val savedCedh = remember(mode) { if (prefs.contains("cedh_current")) runCatching { cedh.load() } else null }
    fun open(key: String) { prefs.edit().putString("active_modality", key).apply(); mode = key }
    BackHandler(mode != null) { mode = null }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = AppSpace.contentWidth).fillMaxSize()) {
                if (mode != null) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (mode) {
                            "dc500" -> Dc500App(onBack = { mode = null }, onSettings = { settings = true })
                            "cedh" -> CedhApp(cedh, onBack = { mode = null }, onSettings = { settings = true })
                            "duplas" -> ChampionshipApp(duplas, onBack = { mode = null }, onSettings = { settings = true })
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(AppSpace.md), verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                        item { ChampionshipHeader(language, onLanguageChange, { settings = true }) }
                        item { SectionHeader(stringResource(R.string.home_formats), stringResource(R.string.home_formats_hint)) }
                        item { ChampionshipCard(stringResource(R.string.modality_duplas), stringResource(R.string.duplas_description), stringResource(R.string.duplas_count), Icons.Rounded.Groups, { open("duplas") }) }
                        item { ChampionshipCard(stringResource(R.string.modality_cedh), stringResource(R.string.cedh_description), stringResource(R.string.cedh_count), Icons.Rounded.AutoAwesome, { open("cedh") }) }
                        item { ChampionshipCard(stringResource(R.string.modality_dc500), stringResource(R.string.dc500_description), stringResource(R.string.dc500_count), Icons.Rounded.Shield, { open("dc500") }) }
                        item { Spacer(Modifier.height(AppSpace.sm)); SectionHeader(stringResource(R.string.saved_tournaments)) }
                        savedTeams?.getOrNull()?.let { saved ->
                            item { ChampionshipCard(saved.name, stringResource(R.string.modality_duplas), stringResource(R.string.continue_tournament), Icons.Rounded.EmojiEvents, { open("duplas") }) }
                        }
                        savedCedh?.getOrNull()?.let { saved ->
                            item { ChampionshipCard(saved.name, stringResource(R.string.modality_cedh), stringResource(R.string.continue_tournament), Icons.Rounded.EmojiEvents, { open("cedh") }) }
                        }
                        if (savedTeams?.isFailure == true || savedCedh?.isFailure == true) {
                            item { EmptyState(stringResource(R.string.cannot_open), stringResource(R.string.saved_read_error)) }
                        } else if (savedTeams == null && savedCedh == null) {
                            item { EmptyState(stringResource(R.string.no_saved_title), stringResource(R.string.no_saved_body)) }
                        }
                        item { Text(stringResource(R.string.dc500_storage_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
    if (settings) AlertDialog(onDismissRequest = { settings = false },
        title = { Text(stringResource(R.string.settings_title)) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Text(stringResource(R.string.language_selector))
            LanguageSelector(language, onLanguageChange)
            Text(stringResource(R.string.local_storage_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }, confirmButton = { PrimaryButton(stringResource(R.string.understood), { settings = false }) })
}
