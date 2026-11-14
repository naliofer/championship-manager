package com.campeonato.duplas.ui.screens
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.campeonato.duplas.data.*
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.campeonato.duplas.R
@Composable
fun ModalityApp(prefs: SharedPreferences) {
    var mode by remember { mutableStateOf(prefs.getString("active_modality",null)) }
    Column(Modifier.fillMaxSize()) {
        Row { TextButton(onClick={mode=null}){Text("Selecionar modalidade")} }
        when(mode) {
            "dc500" -> Dc500App()
            "cedh" -> CedhApp(remember { CedhRepository(prefs) })
            "duplas" -> ChampionshipApp(remember { ChampionshipRepository(prefs) })
            else -> {
                Image(painterResource(R.drawable.championship_logo),contentDescription="Logo Championship Manager",modifier=Modifier.size(112.dp))
                Text(stringResource(R.string.app_name),style=MaterialTheme.typography.headlineSmall)
                Text("Criar ou continuar campeonato")
                listOf("duplas" to "Campeonato de Duplas","cedh" to "cEDH Multiplayer (4 jogadores)","dc500" to "Duel Commander 500 (1?1)").forEach { (key,label) ->
                    Button(onClick={prefs.edit().putString("active_modality",key).apply();mode=key}){Text(label)}
                }
            }
        }
    }
}
