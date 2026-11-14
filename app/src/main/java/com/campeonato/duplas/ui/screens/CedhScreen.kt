package com.campeonato.duplas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.campeonato.duplas.ui.components.*
import androidx.compose.ui.res.stringResource
import com.campeonato.duplas.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campeonato.duplas.data.*
import com.campeonato.duplas.domain.CedhEngine as E
import com.campeonato.duplas.domain.CedhClock
import java.util.UUID
import kotlinx.coroutines.delay

@Composable
private fun CedhField(label: String, value: String, enabled: Boolean = true, change: (String) -> Unit) {
    OutlinedTextField(value,change,label={Text(label)},enabled=enabled,modifier=Modifier.fillMaxWidth(), colors = championshipFieldColors())
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CedhApp(repository: CedhRepository, onBack: () -> Unit = {}, onSettings: () -> Unit = {}) {
    val loaded=remember { runCatching { repository.load() } }
    if(loaded.isFailure) { Text("Dados preservados. Falha ao abrir: ${loaded.exceptionOrNull()?.message}"); return }
    var s by remember { mutableStateOf(loaded.getOrThrow()) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var preview by remember { mutableStateOf<List<E.Table>?>(null) }
    var editing by remember { mutableStateOf<Int?>(null) }
    var reset by remember { mutableStateOf<Int?>(null) }
    var fresh by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while(true) { now=System.currentTimeMillis(); delay(1000) } }
    fun save(next: CedhState) { try { repository.save(next); s=next; error=null } catch(e: Exception) { error=e.message } }
    val locked=s.tables.isNotEmpty()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
        Column {
            TournamentTopBar(s.name, stringResource(R.string.modality_cedh), onBack, onSettings)
            TournamentTabs(listOf(stringResource(R.string.registration), stringResource(R.string.pods), stringResource(R.string.standings)), tab) { tab = it }
        }
    }) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        if(tab==0) {
            CedhField("Nome",s.name,!locked) { save(s.copy(name=it)) }
            fun number(label: String, value: Int, update: (Int)->E.Config): @Composable ()->Unit = {
                var text by remember(value) { mutableStateOf(value.toString()) }
                CedhField(label,text,!locked) { text=it; it.toIntOrNull()?.let { n -> save(s.copy(config=update(n))) } }
            }
            number("Rodadas",s.config.rounds) { s.config.copy(rounds=it) }()
            number("Minutos",s.config.minutes) { s.config.copy(minutes=it) }()
            number("Tempo adicional (min)",s.config.extraMinutes) { s.config.copy(extraMinutes=it) }()
            number("Pontos por vitória",s.config.win) { s.config.copy(win=it) }()
            number("Pontos por empate",s.config.draw) { s.config.copy(draw=it) }()
            number("Pontos por derrota",s.config.loss) { s.config.copy(loss=it) }()
            number("Tempo da final (min)",s.config.finalMinutes) { s.config.copy(finalMinutes=it) }()
            number("Adicional da final (min)",s.config.finalExtraMinutes) { s.config.copy(finalExtraMinutes=it) }()
            Row { Checkbox(s.config.top4,{save(s.copy(config=s.config.copy(top4=it)))},enabled=!locked); Text(stringResource(R.string.top4)) }
            Row { Checkbox(s.config.agreement,{save(s.copy(config=s.config.copy(agreement=it)))},enabled=!locked); Text(stringResource(R.string.agreement)) }
            Text("Desempates: pontos → vitórias → OMW% (adversários únicos).")
            s.players.forEach { p ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    CedhField("Jogador",p.name,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(name=v) else it })) }
                    CedhField("Comandante(s)",p.commanders,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(commanders=v) else it })) }
                    CedhField("Deck",p.deck,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(deck=v) else it })) }
                    CedhField("Moxfield (opcional)",p.moxfield,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(moxfield=v) else it })) }
                    TextButton(onClick={save(s.copy(players=s.players.filter { it.id!=p.id }))},enabled=!locked, colors=destructiveTextColors()) { Text(stringResource(R.string.remove)) }
                } }
            }
            Button(onClick={save(s.copy(players=s.players+E.Player(UUID.randomUUID().toString(),"")))},enabled=!locked, colors = championshipButtonColors()) { Text("Adicionar jogador (${s.players.size})") }
            Button(onClick={fresh=true}, colors = championshipButtonColors()) { Text(stringResource(R.string.new_cedh)) }
            var archives by remember { mutableStateOf<List<CedhState>>(emptyList()) }
            TextButton(onClick={try{archives=repository.archives()}catch(e:Exception){error=e.message}}) { Text(stringResource(R.string.archived)) }
            archives.forEach { old -> TextButton(onClick={try{repository.archive(s);save(old);archives=emptyList()}catch(e:Exception){error=e.message}}) { Text("Abrir: ${old.name} (${old.players.size} jogadores)") } }
        }
        if(tab==1) {
            val round=s.tables.maxOfOrNull { it.round } ?: 0
            val complete=locked && s.tables.all { it.result!=null }
            Button(onClick={try {
                require(s.players.all { it.deck.isNotBlank() && it.commanders.isNotBlank() }) { "Preencha deck e comandantes." }
                preview=if(round<s.config.rounds) E.pair(s.players,s.tables,s.config) else listOf(E.finalTable(s.players,s.tables,s.config))
            }catch(e:Exception){error=e.message}},enabled=(!locked || complete) && (round<s.config.rounds || (s.config.top4 && round==s.config.rounds)), colors = championshipButtonColors()) { Text(stringResource(R.string.preview_next)) }
            preview?.let { tables ->
                Text(stringResource(R.string.preview_confirm))
                tables.forEachIndexed { i,t -> Text("Mesa ${i+1}: "+t.seats.joinToString(" → ") { id -> s.players.first { it.id==id }.name }) }
                Button(onClick={ val next=s.copy(tables=s.tables+tables); try { E.validate(next.players,next.tables,next.config); save(next); preview=null }catch(e:Exception){error=e.message} }, colors = championshipButtonColors()) { Text(stringResource(R.string.confirm_pairings)) }
                TextButton(onClick={preview=null}) { Text(stringResource(R.string.cancel_preview)) }
            }
            s.tables.groupBy { it.round }.toSortedMap(reverseOrder()).forEach { (r,tables) ->
                var expanded by remember(r) { mutableStateOf(r==round) }
                TextButton(onClick={expanded=!expanded}) { Text(if(tables.first().final) "Final Top 4" else "Rodada $r") }
                if(expanded) {
                    val clock=s.clocks[r] ?: CedhClock()
                    val elapsed=clock.elapsedAt(now)/1000
                    val minutes=if(tables.first().final)s.config.finalMinutes else s.config.minutes
                    val extra=if(tables.first().final)s.config.finalExtraMinutes else s.config.extraMinutes
                    val remaining=minutes*60-elapsed
                    Text(if(remaining>0) "Tempo: ${remaining/60}:${(remaining%60).toString().padStart(2,'0')}" else "Tempo regulamentar encerrado. Jogador ativo conclui o turno. Adicional: ${(extra*60+remaining).coerceAtLeast(0)} s",color=if(remaining<=0)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    if(remaining<=-extra*60) Text(if(tables.first().final) "Final: continue até haver um vencedor." else "Limite adicional encerrado. Sem vencedor, registre empate por tempo.")
                    FlowRow {
                        TextButton(onClick={save(s.copy(clocks=s.clocks+(r to if(clock.since==null)clock.start(System.currentTimeMillis()) else clock.pause(System.currentTimeMillis()))))}) { Text(if(clock.since==null) "Iniciar / retomar" else "Pausar") }
                        TextButton(onClick={reset=r}) { Text(stringResource(R.string.restart)) }
                    }
                    tables.forEachIndexed { i,t -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text("Mesa ${i+1}")
                        t.seats.forEachIndexed { seat,id -> val p=s.players.first { it.id==id }; Text("${seat+1}. ${p.name} · ${p.commanders} · ${p.deck}") }
                        Text(t.result?.let { it.winner?.let { id -> "Vitória: ${s.players.first { it.id==id }.name}" } ?: it.outcome.name } ?: "Pendente")
                        t.result?.let { Text("${it.durationSeconds}s · ${it.notes}") }
                        Button(onClick={editing=s.tables.indexOf(t)}, colors = championshipButtonColors()) { Text(if(t.result==null) "Registrar resultado" else "Corrigir resultado") }
                    } } }
                }
            }
        }
        if(tab==2) {
            val ranking=E.standings(s.players,s.tables,s.config)
            val finished=locked && s.tables.all { it.result!=null } && (s.tables.any { it.final } || (!s.config.top4 && s.tables.maxOf { it.round }==s.config.rounds))
            val champion=if(s.config.top4)s.tables.firstOrNull { it.final }?.result?.winner else ranking.firstOrNull()?.id
            if(finished && champion!=null) Text("Campeão: ${s.players.first { it.id==champion }.name}",style=MaterialTheme.typography.headlineSmall)
            if(s.config.top4)Text(stringResource(R.string.qualifying_standings))
            ranking.forEachIndexed { i,row -> val p=s.players.first { it.id==row.id }
                Card(Modifier.fillMaxWidth()) { Box(Modifier.padding(16.dp)) { Text("${i+1}. ${p.name} · ${p.commanders} / ${p.deck}\n${row.points} pts · ${row.wins}V ${row.draws}E ${row.losses}D · ${row.games} jogos · ${"%.1f".format(row.winRate*100)}% V · OMW ${"%.1f".format(row.omw*100)}%") } }
            }
        }
    }
    }
    reset?.let { r -> AlertDialog(onDismissRequest={reset=null},title={Text(stringResource(R.string.reset_clock))},text={Text(stringResource(R.string.reset_clock_body))},confirmButton={TextButton(onClick={save(s.copy(clocks=s.clocks+(r to CedhClock())));reset=null}){Text(stringResource(R.string.restart))}},dismissButton={TextButton(onClick={reset=null}){Text(stringResource(R.string.cancel))}}) }
    if(fresh) AlertDialog(onDismissRequest={fresh=false},title={Text(stringResource(R.string.archive_create))},confirmButton={TextButton(onClick={try{repository.archive(s);save(CedhState());fresh=false}catch(e:Exception){error=e.message}}){Text(stringResource(R.string.create))}},dismissButton={TextButton(onClick={fresh=false}){Text(stringResource(R.string.cancel))}})
    editing?.let { index -> CedhResultDialog(s.tables[index],s.players,s.config,onDismiss={editing=null}) { result ->
        val next=s.copy(tables=s.tables.mapIndexed { i,t -> if(i==index)t.copy(result=result) else t })
        try { E.validate(next.players,next.tables,next.config);save(next);editing=null }catch(e:Exception){error=e.message}
    } }
}
@Composable
private fun CedhResultDialog(table: E.Table, players: List<E.Player>, config: E.Config, onDismiss:()->Unit, onSave:(E.Result)->Unit) {
    var winner by remember { mutableStateOf(table.result?.winner) }
    var outcome by remember { mutableStateOf(table.result?.outcome ?: E.Outcome.TIME_DRAW) }
    var notes by remember { mutableStateOf(table.result?.notes ?: "") }
    var duration by remember { mutableStateOf((table.result?.durationSeconds ?: 0).toString()) }
    var eliminated by remember { mutableStateOf(table.result?.eliminated ?: emptyMap()) }
    var penalties by remember { mutableStateOf(table.result?.penalties ?: emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest=onDismiss,title={Text(stringResource(R.string.pod_result))},text={Column(Modifier.verticalScroll(rememberScrollState())) {
        table.seats.forEach { id ->
            val p=players.first { it.id==id }
            TextButton(onClick={winner=id;outcome=E.Outcome.WIN;eliminated=eliminated-id}){Text((if(winner==id)"✓ " else "")+"Vitória: ${p.name}")}
            Row { Checkbox(id in eliminated,{eliminated=if(it)eliminated+(id to "") else eliminated-id},enabled=winner!=id);Text("${p.name} eliminado") }
            if(id in eliminated)CedhField("Causa (opcional)",eliminated.getValue(id)){eliminated=eliminated+(id to it)}
            CedhField("Ajuste administrativo: ${p.name}",(penalties[id] ?: 0).toString()){v->v.toIntOrNull()?.let {penalties=penalties+(id to it)}}
        }
        listOf(E.Outcome.TIME_DRAW,E.Outcome.ADMINISTRATIVE).plus(if(config.agreement)listOf(E.Outcome.AGREEMENT_DRAW) else emptyList()).forEach { o -> TextButton(onClick={winner=null;outcome=o}){Text((if(outcome==o)"✓ " else "")+o.name)} }
        CedhField("Duração (segundos)",duration){duration=it}
        CedhField("Observações",notes){notes=it}
        error?.let {Text(it)}
    }},confirmButton={TextButton(onClick={val seconds=duration.toIntOrNull();if(seconds==null||seconds<0)error="Duração inválida" else if(table.final && winner==null)error="A final exige vencedor; continue a mesa conforme procedimento anunciado." else onSave(E.Result(outcome,winner,eliminated,seconds,notes,penalties))}){Text(stringResource(R.string.save))}},dismissButton={TextButton(onClick=onDismiss){Text(stringResource(R.string.cancel))}})
}
