package com.campeonato.duplas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.campeonato.duplas.Language
import com.campeonato.duplas.R
import com.campeonato.duplas.ui.theme.*

@Composable
fun LanguageSelector(language: String, onLanguageChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val flags = listOf(R.drawable.flag_br, R.drawable.flag_us, R.drawable.flag_es)
    val names = listOf(R.string.language_br, R.string.language_us, R.string.language_es)
    val index = Language.tags.indexOf(language).coerceAtLeast(0)
    Box {
        OutlinedButton(onClick = { expanded = true }, contentPadding = PaddingValues(AppSpace.sm), colors = championshipSecondaryColors()) {
            Image(painterResource(flags[index]), null, Modifier.size(28.dp, 20.dp))
            Spacer(Modifier.width(AppSpace.sm))
            Text(stringResource(R.string.language_short))
            Icon(Icons.Rounded.ExpandMore, null)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            Language.tags.forEachIndexed { i, tag ->
                DropdownMenuItem(
                    text = { Text(stringResource(names[i])) },
                    leadingIcon = { Image(painterResource(flags[i]), null, Modifier.size(28.dp, 20.dp)) },
                    trailingIcon = { if (tag == language) Icon(Icons.Rounded.Check, stringResource(R.string.selected_language)) },
                    onClick = { onLanguageChange(tag); expanded = false })
            }
        }
    }
}

@Composable
fun ChampionshipHeader(language: String, onLanguageChange: (String) -> Unit, onSettings: () -> Unit, compact: Boolean = false) {
    Column(Modifier.fillMaxWidth().padding(horizontal = AppSpace.md, vertical = AppSpace.sm), verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Image(painterResource(R.drawable.championship_logo), null, Modifier.size(if (compact) 40.dp else 56.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge)
                if (!compact) Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings_title)) }
        }
        if (!compact) LanguageSelector(language, onLanguageChange)
    }
}

@Composable
fun ChampionshipCard(title: String, description: String, detail: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Card(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = AppSpace.cardMinHeight),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(AppDepth.border, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDepth.card)) {
        Column(Modifier.fillMaxWidth().padding(AppSpace.lg), verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(icon, null, Modifier.padding(AppSpace.sm).size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatusIndicator(if (enabled) detail else stringResource(R.string.in_development))
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun EmptyState(title: String, description: String) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(AppSpace.lg), verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            Icon(Icons.Rounded.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.heightIn(min = AppSpace.touchTarget), enabled = enabled, colors = championshipButtonColors()) { Text(text) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick, modifier.heightIn(min = AppSpace.touchTarget), colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text(text) }
}

@Composable
fun ConfirmationDialog(title: String, message: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) },
        confirmButton = { PrimaryButton(confirm, onConfirm) },
        dismissButton = { SecondaryButton(stringResource(R.string.cancel), onDismiss) })
}

@Composable
fun StatusIndicator(text: String) {
    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = AppDepth.selectedAlpha), shape = MaterialTheme.shapes.small) {
        Text(text, Modifier.padding(horizontal = AppSpace.sm, vertical = AppSpace.xs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
