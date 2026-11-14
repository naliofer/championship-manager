# Invent?rio inicial de internacionaliza??o

Status: implementa??o pendente. Escrita em web bloqueada na cria??o de arquivo tempor?rio; nenhuma tradu??o foi aplicada.

## Arquitetura

- Android: Compose em ChampionshipScreen.kt (Duplas), CedhScreen.kt e ModalityScreen.kt. Dc500Screen.kt hospeda os m?dulos web locais. Prefer?ncias existentes: SharedPreferences.
- PWA: bootstrap.js seleciona app.js, cedh-app.js ou dc500-app.js. Cada modalidade tem motor e armazenamento pr?prios. DC500 ? compartilhado com Android.
- N?o foram encontrados cat?logos strings.xml nas pastas values inspecionadas.
- Sem tradu??o centralizada existente identificada nos m?dulos de interface.

## Escopo necess?rio

Extrair textos de interface, valida??o dos motores e persist?ncia; separar mensagens de dados do usu?rio. Datas, percentuais, moeda BRL e plurais precisam usar locale selecionado. O m?dulo compartilhado DC500 deve receber a prefer?ncia Android sem alterar os dados do torneio.

## Candidatos por arquivo

Invent?rio lexical para revis?o humana. Inclui identificadores, URLs e dados internos; as contagens abaixo N?O s?o quantidades de mensagens traduz?veis nem tradu??es conclu?das. Interpola??es e templates HTML exigem extra??o contextual.

### app/src/main/java/com/campeonato/duplas/MainActivity.kt ? 1 linhas candidatas

```text
13: val prefs = getSharedPreferences("championship", Activity.MODE_PRIVATE)
```

### app/src/main/java/com/campeonato/duplas/data/CedhRepository.kt ? 32 linhas candidatas

```text
10: data class CedhState(val id: String = UUID.randomUUID().toString(), val name: String = "cEDH Multiplayer",
17: put("version", 1); put("id", s.id); put("name", s.name)
18: put("config", JSONObject().apply { val c = s.config
19: put("rounds", c.rounds); put("minutes", c.minutes); put("extra", c.extraMinutes)
20: put("win", c.win); put("draw", c.draw); put("loss", c.loss); put("top4", c.top4); put("agreement", c.agreement)
21: put("finalMinutes",c.finalMinutes); put("finalExtra",c.finalExtraMinutes)
23: put("players", JSONArray().apply { s.players.forEach { p -> put(JSONObject().apply {
24: put("id",p.id); put("name",p.name); put("commanders",p.commanders); put("deck",p.deck); put("moxfield",p.moxfield)
26: put("tables", JSONArray().apply { s.tables.forEach { t -> put(JSONObject().apply {
27: put("round",t.round); put("seats",JSONArray(t.seats)); put("final",t.final)
28: t.result?.let { r -> put("result",JSONObject().apply {
29: put("outcome",r.outcome.name); put("winner",r.winner); put("eliminated",JSONObject(r.eliminated))
30: put("duration",r.durationSeconds); put("notes",r.notes); put("penalties",JSONObject(r.penalties))
33: put("clocks",JSONObject().apply { s.clocks.forEach { (round,c) -> put(round.toString(),JSONObject().put("elapsed",c.elapsed).put("since",c.since)) } })
36: val j=JSONObject(raw); require(j.getInt("version")==1)
37: val c=j.getJSONObject("config")
38: val config=E.Config(c.getInt("rounds"),c.getInt("minutes"),c.getInt("extra"),c.getInt("win"),c.getInt("draw"),c.getInt("loss"),c.getBoolean("top4"),c.getBoolean("agreement"),c.optInt("finalMinutes",75),c.optInt("finalExtra",10))
39: val players=j.getJSONArray("players").objects().map { E.Player(it.getString("id"),it.getString("name"),it.getString("commanders"),it.getString("deck"),it.getString("moxfield")) }
40: val tables=j.getJSONArray("tables").objects().map { t ->
41: val seats=t.getJSONArray("seats"); val r=t.optJSONObject("result")
42: E.Table(t.getInt("round"),(0 until seats.length()).map { seats.getString(it) },r?.let {
43: E.Result(E.Outcome.valueOf(it.getString("outcome")),if(it.has("winner"))it.getString("winner") else null,
44: it.getJSONObject("eliminated").strings(),it.getInt("duration"),it.getString("notes"),
45: it.getJSONObject("penalties").let { p -> p.keys().asSequence().associateWith { k -> p.getInt(k) } })
46: },t.getBoolean("final"))
48: val clocks=j.getJSONObject("clocks").let { cs -> cs.keys().asSequence().associate { key ->
49: val v=cs.getJSONObject(key); key.toInt() to CedhClock(v.getLong("elapsed"),if(v.has("since"))v.getLong("since") else null)
51: return CedhState(j.getString("id"),j.getString("name"),config,players,tables,clocks).also(::validate)
64: fun archives(): List<CedhState> = prefs.all.keys.filter { it.startsWith("cedh_archive_") }.mapNotNull { prefs.getString(it,null)?.let(CedhCodec::decode) }
65: fun load(): CedhState = prefs.getString("cedh_current",null)?.let(CedhCodec::decode) ?: CedhState()
66: fun save(s: CedhState) { CedhCodec.validate(s); check(prefs.edit().putString("cedh_current",CedhCodec.encode(s)).commit()) { "Falha ao salvar cEDH" } }
67: fun archive(s: CedhState) { check(prefs.edit().putString("cedh_archive_${s.id}",CedhCodec.encode(s)).commit()) }
```

### app/src/main/java/com/campeonato/duplas/data/ChampionshipRepository.kt ? 34 linhas candidatas

```text
12: check(prefs.edit().putString("archive_${System.currentTimeMillis()}", ChampionshipCodec.encode(state)).commit()) {
13: "Não foi possível preservar o campeonato anterior."
17: val snapshot = prefs.getString("championship_v2", null)
23: check(prefs.edit().putString("championship_v2", ChampionshipCodec.encode(state)).commit()) {
24: "Não foi possível salvar no celular. Tente novamente antes de sair."
29: val rawTeams = prefs.getString("teams", null)
30: val rawNames = prefs.getString("names", null)
31: val rawMatches = prefs.getString("matches", null)
38: val a = Player("legacy-${i}-a", j.optString("a"))
39: val b = Player("legacy-${i}-b", j.optString("b"))
46: val a = Player("legacy-${i}-a", arr.getString(i))
47: val b = Player("legacy-${i}-b")
55: val match = Match(j.getInt("r"), j.getInt("a"), j.getInt("b"), j.getInt("v"),
56: j.optInt("la").coerceIn(0, 2), j.optInt("lb").coerceIn(0, 2), legacyScore = true)
68: put("version", 2)
69: put("name", state.name); put("rounds", state.rounds)
70: put("created", state.created); put("started", state.started); put("confirmed", state.teamsConfirmed)
71: put("players", JSONArray().apply { state.players.forEach { put(JSONObject().put("id", it.id).put("name", it.name).put("deck", it.deck)) } })
72: put("teams", JSONArray().apply { state.teams.forEach { put(JSONObject().put("a", it.playerA).put("b", it.playerB)) } })
73: put("matches", JSONArray().apply { state.matches.forEach { m -> put(JSONObject().apply {
74: put("round", m.round); put("a", m.a); put("b", m.b); put("result", m.result)
75: put("lossesA", m.lossesA); put("lossesB", m.lossesB); put("legacy", m.legacyScore)
76: put("events", JSONArray().apply { m.eliminations.forEach { put(JSONObject().put("killer", it.killerId).put("victim", it.victimId)) } })
82: require(root.getInt("version") == 2) { "Versão de dados não suportada. Os dados salvos foram preservados." }
83: val players = root.getJSONArray("players").mapObjects { Player(it.getString("id"), it.getString("name"), it.getString("deck")) }
85: val teams = root.getJSONArray("teams").mapObjects {
86: val a = byId.getValue(it.getString("a")); val b = byId.getValue(it.getString("b"))
89: val matches = root.getJSONArray("matches").mapObjects { m ->
90: Match(m.getInt("round"), m.getInt("a"), m.getInt("b"), m.getInt("result"), m.getInt("lossesA"), m.getInt("lossesB"),
91: m.getJSONArray("events").mapObjects { Elimination(it.getString("killer"), it.getString("victim")) }, m.getBoolean("legacy"))
93: return Championship(root.getString("name"), root.getInt("rounds"), players, teams, matches,
94: root.getBoolean("confirmed"), root.getBoolean("started"), root.getBoolean("created")).also(::validate)
107: require(pairs.distinct().size == pairs.size) { "Confrontos duplicados nos dados salvos." }
109: require(games.size == state.teams.size / 2) { "Rodada incompleta nos dados salvos." }
```

### app/src/main/java/com/campeonato/duplas/domain/CedhEngine.kt ? 2 linhas candidatas

```text
7: data class Player(val id: String, val name: String, val commanders: String = "", val deck: String = "", val moxfield: String = "")
13: val durationSeconds: Int = 0, val notes: String = "", val penalties: Map<String, Int> = emptyMap())
```

### app/src/main/java/com/campeonato/duplas/domain/ChampionshipEngine.kt ? 23 linhas candidatas

```text
11: }?.ifBlank { "Dupla ${index + 1}" } ?: "Dupla ${index + 1}"
13: fun normalizedDeck(deck: String): String = deck.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
22: players.size !in 4..24 || players.size % 4 != 0 -> "Cadastre 4 a 24 participantes, em múltiplos de 4, para formar um número par de duplas."
23: players.map { it.id }.distinct().size != players.size -> "Existem participantes duplicados."
24: players.any { it.name.isBlank() } -> "Preencha o nome de todos os participantes."
25: players.any { normalizedDeck(it.deck).isBlank() } -> "O deck é obrigatório para todos os participantes."
44: "Não é possível formar todas as duplas: mais da metade dos participantes usa o mesmo deck."
46: return requireNotNull(search(players.shuffled(random))) { "Não há combinação de duplas com decks diferentes." }.shuffled(random)
60: require(state.created && !state.started) { "O campeonato já começou ou ainda não foi criado." }
61: require(state.teamsConfirmed && validTeams(state)) { "Confirme duplas válidas antes de iniciar." }
62: require(state.rounds in 1..maxRounds(state.teams.size)) { "Quantidade de rodadas inválida." }
67: require(!state.started) { "A configuração está bloqueada após o início do campeonato." }
68: require(name.isNotBlank()) { "Informe o nome do campeonato." }
69: require(rounds in 1..maxRounds(state.players.size / 2)) { "Quantidade de rodadas inválida para o número de duplas." }
74: require(!state.started) { "Os participantes estão bloqueados após o início do campeonato." }
115: require(state.started) { "Inicie o campeonato antes de sortear os confrontos." }
116: require(state.matches.all { it.result != -1 }) { "Confirme todos os resultados antes de sortear." }
118: require(round <= state.rounds) { "Todas as rodadas configuradas já foram realizadas." }
119: val pairs = requireNotNull(makePairs(state.matches, state.teams.size)) { "Não é possível gerar novos confrontos sem repetir adversários." }
124: require(match.a != match.b && match.a in teams.indices && match.b in teams.indices) { "Confronto inválido." }
127: require(events.map { it.victimId }.distinct().size == events.size) { "Cada jogador pode ser eliminado apenas uma vez por confronto." }
129: "Uma eliminação deve ocorrer entre adversários; não é permitido eliminar a si mesmo ou seu parceiro."
134: require(state.started) { "O campeonato ainda não começou." }
```

### app/src/main/java/com/campeonato/duplas/model/Models.kt ? 4 linhas candidatas

```text
3: data class Player(val id: String, val name: String = "", val deck: String = "")
4: data class Team(val personA: String, val personB: String, val playerA: String = "", val playerB: String = "") {
16: val name: String = "Damian House - Champ", val rounds: Int = 4,
17: val players: List<Player> = (1..12).map { Player("player-$it") },
```

### app/src/main/java/com/campeonato/duplas/ui/components/AppComponents.kt ? 22 linhas candidatas

```text
109: Text("Dupla ${index + 1}", style = MaterialTheme.typography.titleMedium)
115: label = { Text("Pessoa 1") },
123: label = { Text("Pessoa 2") },
129: "Nome da dupla: $label",
157: Text("$labelA × $labelB", style = MaterialTheme.typography.titleMedium)
158: Text("Placar: ${match.lossesB} × ${match.lossesA}", style = MaterialTheme.typography.bodyLarge)
180: label = { Text("Resultado: $derivedLabel") },
189: Text(if (isConfirmed) "Atualizar" else "Confirmar")
196: Text("Confirmado", color = AppWin, style = MaterialTheme.typography.labelLarge)
202: "Derrotas individuais: $labelA = ${match.lossesA} | $labelB = ${match.lossesB}",
225: IconButton(onClick = onDecrease) { Text("−", style = MaterialTheme.typography.titleLarge) }
227: IconButton(onClick = onIncrease) { Text("+", style = MaterialTheme.typography.titleLarge) }
236: val alpha by animateFloatAsState(targetValue = if (isChampion) 1f else 0.95f, label = "rankAlpha")
267: Text("${position + 1}", style = MaterialTheme.typography.labelLarge, color = accent)
272: Text("${position + 1}º • $label", style = MaterialTheme.typography.titleMedium)
274: "${standing.points} pontos • ${standing.wins} vitórias • ${standing.draws} empates • ${standing.losses} derrotas",
278: Text("Pessoais: ${standing.individualWins} eliminações realizadas • ${standing.individualLosses} sofridas", style = MaterialTheme.typography.bodySmall)
279: Text("Buchholz: ${standing.buchholz}", style = MaterialTheme.typography.bodySmall, color = AppMutedText)
288: 0, 1 -> "Vitória" to AppWin
289: 2 -> "Empate" to AppChampion
290: else -> "Pendente" to AppLoss
319: "Rodada $round",
```

### app/src/main/java/com/campeonato/duplas/ui/screens/CedhScreen.kt ? 53 linhas candidatas

```text
23: if(loaded.isFailure) { Text("Dados preservados. Falha ao abrir: ${loaded.exceptionOrNull()?.message}"); return }
37: Row { listOf("Cadastro","Mesas","Classificação").forEachIndexed { i,title -> TextButton(onClick={tab=i}) { Text(title) } } }
40: CedhField("Nome",s.name,!locked) { save(s.copy(name=it)) }
45: number("Rodadas",s.config.rounds) { s.config.copy(rounds=it) }()
46: number("Minutos",s.config.minutes) { s.config.copy(minutes=it) }()
47: number("Tempo adicional (min)",s.config.extraMinutes) { s.config.copy(extraMinutes=it) }()
48: number("Pontos por vitória",s.config.win) { s.config.copy(win=it) }()
49: number("Pontos por empate",s.config.draw) { s.config.copy(draw=it) }()
50: number("Pontos por derrota",s.config.loss) { s.config.copy(loss=it) }()
51: number("Tempo da final (min)",s.config.finalMinutes) { s.config.copy(finalMinutes=it) }()
52: number("Adicional da final (min)",s.config.finalExtraMinutes) { s.config.copy(finalExtraMinutes=it) }()
53: Row { Checkbox(s.config.top4,{save(s.copy(config=s.config.copy(top4=it)))},enabled=!locked); Text("Final Top 4") }
54: Row { Checkbox(s.config.agreement,{save(s.copy(config=s.config.copy(agreement=it)))},enabled=!locked); Text("Permitir empate por acordo") }
55: Text("Desempates: pontos → vitórias → OMW% (adversários únicos).")
58: CedhField("Jogador",p.name,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(name=v) else it })) }
59: CedhField("Comandante(s)",p.commanders,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(commanders=v) else it })) }
60: CedhField("Deck",p.deck,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(deck=v) else it })) }
61: CedhField("Moxfield (opcional)",p.moxfield,!locked) { v -> save(s.copy(players=s.players.map { if(it.id==p.id)it.copy(moxfield=v) else it })) }
62: TextButton(onClick={save(s.copy(players=s.players.filter { it.id!=p.id }))},enabled=!locked) { Text("Remover") }
65: Button(onClick={save(s.copy(players=s.players+E.Player(UUID.randomUUID().toString(),"")))},enabled=!locked) { Text("Adicionar jogador (${s.players.size})") }
66: Button(onClick={fresh=true}) { Text("Novo campeonato cEDH") }
68: TextButton(onClick={try{archives=repository.archives()}catch(e:Exception){error=e.message}}) { Text("Campeonatos arquivados") }
69: archives.forEach { old -> TextButton(onClick={try{repository.archive(s);save(old);archives=emptyList()}catch(e:Exception){error=e.message}}) { Text("Abrir: ${old.name} (${old.players.size} jogadores)") } }
75: require(s.players.all { it.deck.isNotBlank() && it.commanders.isNotBlank() }) { "Preencha deck e comandantes." }
77: }catch(e:Exception){error=e.message}},enabled=(!locked || complete) && (round<s.config.rounds || (s.config.top4 && round==s.config.rounds))) { Text("Prévia da próxima rodada") }
79: Text("Prévia — confirme para salvar")
80: tables.forEachIndexed { i,t -> Text("Mesa ${i+1}: "+t.seats.joinToString(" → ") { id -> s.players.first { it.id==id }.name }) }
81: Button(onClick={ val next=s.copy(tables=s.tables+tables); try { E.validate(next.players,next.tables,next.config); save(next); preview=null }catch(e:Exception){error=e.message} }) { Text("Confirmar sorteio") }
82: TextButton(onClick={preview=null}) { Text("Cancelar prévia") }
86: TextButton(onClick={expanded=!expanded}) { Text(if(tables.first().final) "Final Top 4" else "Rodada $r") }
93: Text(if(remaining>0) "Tempo: ${remaining/60}:${(remaining%60).toString().padStart(2,'0')}" else "Tempo regulamentar encerrado. Jogador ativo conclui o turno. Adicional: ${(extra*60+remaining).coerceAtLeast(0)} s",color=if(remaining<=0)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
94: if(remaining<=-extra*60) Text(if(tables.first().final) "Final: continue até haver um vencedor." else "Limite adicional encerrado. Sem vencedor, registre empate por tempo.")
96: TextButton(onClick={save(s.copy(clocks=s.clocks+(r to if(clock.since==null)clock.start(System.currentTimeMillis()) else clock.pause(System.currentTimeMillis()))))}) { Text(if(clock.since==null) "Iniciar / retomar" else "Pausar") }
97: TextButton(onClick={reset=r}) { Text("Reiniciar") }
100: Text("Mesa ${i+1}")
101: t.seats.forEachIndexed { seat,id -> val p=s.players.first { it.id==id }; Text("${seat+1}. ${p.name} · ${p.commanders} · ${p.deck}") }
102: Text(t.result?.let { it.winner?.let { id -> "Vitória: ${s.players.first { it.id==id }.name}" } ?: it.outcome.name } ?: "Pendente")
103: t.result?.let { Text("${it.durationSeconds}s · ${it.notes}") }
104: Button(onClick={editing=s.tables.indexOf(t)}) { Text(if(t.result==null) "Registrar resultado" else "Corrigir resultado") }
113: if(finished && champion!=null) Text("Campeão: ${s.players.first { it.id==champion }.name}",style=MaterialTheme.typography.headlineSmall)
114: if(s.config.top4)Text("Classificação classificatória (anterior à final)")
116: Text("${i+1}. ${p.name} · ${p.commanders} / ${p.deck}\n${row.points} pts · ${row.wins}V ${row.draws}E ${row.losses}D · ${row.games} jogos · ${"%.1f".format(row.winRate*100)}% V · OMW ${"%.1f".format(row.omw*100)}%")
120: reset?.let { r -> AlertDialog(onDismissRequest={reset=null},title={Text("Reiniciar cronômetro?")},text={Text("O tempo transcorrido desta rodada será zerado.")},confirmButton={TextButton(onClick={save(s.copy(clocks=s.clocks+(r to CedhClock())));reset=null}){Text("Reiniciar")}},dismissButton={TextButton(onClick={reset=null}){Text("Cancelar")}}) }
121: if(fresh) AlertDialog(onDismissRequest={fresh=false},title={Text("Arquivar e criar campeonato?")},confirmButton={TextButton(onClick={try{repository.archive(s);save(CedhState());fresh=false}catch(e:Exception){error=e.message}}){Text("Criar")}},dismissButton={TextButton(onClick={fresh=false}){Text("Cancelar")}})
136: AlertDialog(onDismissRequest=onDismiss,title={Text("Resultado da mesa")},text={Column(Modifier.verticalScroll(rememberScrollState())) {
139: TextButton(onClick={winner=id;outcome=E.Outcome.WIN;eliminated=eliminated-id}){Text((if(winner==id)"✓ " else "")+"Vitória: ${p.name}")}
140: Row { Checkbox(id in eliminated,{eliminated=if(it)eliminated+(id to "") else eliminated-id},enabled=winner!=id);Text("${p.name} eliminado") }
141: if(id in eliminated)CedhField("Causa (opcional)",eliminated.getValue(id)){eliminated=eliminated+(id to it)}
142: CedhField("Ajuste administrativo: ${p.name}",(penalties[id] ?: 0).toString()){v->v.toIntOrNull()?.let {penalties=penalties+(id to it)}}
144: listOf(E.Outcome.TIME_DRAW,E.Outcome.ADMINISTRATIVE).plus(if(config.agreement)listOf(E.Outcome.AGREEMENT_DRAW) else emptyList()).forEach { o -> TextButton(onClick={winner=null;outcome=o}){Text((if(outcome==o)"✓ " else "")+o.name)} }
145: CedhField("Duração (segundos)",duration){duration=it}
146: CedhField("Observações",notes){notes=it}
148: }},confirmButton={TextButton(onClick={val seconds=duration.toIntOrNull();if(seconds==null||seconds<0)error="Duração inválida" else if(table.final && winner==null)error="A final exige vencedor; continue a mesa conforme procedimento anunciado." else onSave(E.Result(outcome,winner,eliminated,seconds,notes,penalties))}){Text("Salvar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
```

### app/src/main/java/com/campeonato/duplas/ui/screens/ChampionshipScreen.kt ? 77 linhas candidatas

```text
34: Text("Não foi possível abrir os dados", style = MaterialTheme.typography.titleLarge)
35: Text("Os dados locais foram preservados. Não será criado um campeonato por cima deles.")
36: Text(loaded.exceptionOrNull()?.message ?: "Erro de leitura")
54: } catch (e: Exception) { error = e.message ?: "Não foi possível salvar a alteração." }
59: val tabs = listOf("Cadastro", "Duplas", "Confrontos", "Classificação", "Ranking Killer")
72: SectionTitle(if (state.created) "Configuração do campeonato" else "Criar campeonato", "Dados salvos automaticamente neste celular")
79: SectionTitle("Participantes (${state.players.size})", "Padrão: 12 jogadores e 6 duplas. Cada jogador precisa de nome e deck.")
80: if (!state.started) Text("Editar participantes desfaz o sorteio e a confirmação das duplas.")
92: }, enabled = state.players.size < 24) { Text("Adicionar participante") }
96: }) { Text(if (state.teams.isEmpty()) "Sortear duplas" else "Refazer sorteio das duplas") }
101: OutlinedButton(onClick = { newChampionship = true }, enabled = state.created) { Text("Novo campeonato") }
105: item { SectionTitle("Duplas", if (state.started) "Duplas confirmadas — campeonato iniciado" else "Confira integrantes e decks antes de iniciar") }
106: if (state.teams.isEmpty()) item { EmptyStateCard("Cadastre os participantes e sorteie as duplas na aba Cadastro.") }
111: Text("Dupla ${index + 1}", style = MaterialTheme.typography.titleMedium)
112: team.playerIds.forEach { id -> state.players.find { it.id == id }?.let { Text("${it.name.ifBlank { "Nome pendente" }} • ${it.deck.ifBlank { "Deck não informado" }}") } }
117: OutlinedButton(onClick = { update { it.copy(teams = Engine.drawTeams(it.players), teamsConfirmed = false) } }) { Text("Refazer sorteio") }
119: update { require(Engine.validTeams(it)) { "Duplas inválidas. Confira nomes, decks e participantes." }; it.copy(teamsConfirmed = true) }
120: }, enabled = !state.teamsConfirmed) { Text(if (state.teamsConfirmed) "Duplas confirmadas" else "Confirmar duplas") }
121: Button(onClick = { update(Engine::start); if (state.started) tab = 2 }, enabled = state.teamsConfirmed) { Text("Iniciar campeonato") }
126: SectionTitle("Confrontos", "$current de ${state.rounds} rodadas • adversários sem repetição")
127: Text("Vitória: 2×0, 0×2, 2×1 ou 1×2. Demais placares são empate, inclusive 2×2.")
129: Text(if (current == 0) "Sortear 1ª rodada" else "Sortear próxima rodada")
131: if (!state.started) Text("Confirme as duplas e inicie o campeonato na aba Duplas.")
132: if (finished) Text("Campeonato encerrado", color = AppChampion)
134: if (state.matches.isEmpty()) item { EmptyStateCard("Nenhum confronto sorteado.") }
136: item(key = "round-$round") {
143: item { Text("Corrigir uma rodada anterior recalcula as estatísticas, mas mantém os confrontos já sorteados.", style = MaterialTheme.typography.bodySmall) }
146: item { SectionTitle("Classificação geral", "Pontos → vitórias → menos mortes → kills → confronto direto → Buchholz") }
147: if (standings.isEmpty()) item { EmptyStateCard("A classificação ficará disponível após o sorteio das duplas.") }
152: item { Text("Confronto direto é aplicado quando todas as duplas do grupo empatado já se enfrentaram.", style = MaterialTheme.typography.bodySmall) }
156: SectionTitle("Ranking Killer", "Kills → menos mortes → saldo. Empates compartilham a posição.")
157: if (state.matches.any { it.legacyScore && it.result != -1 }) Text("Partidas antigas sem autoria das eliminações não entram no Ranking Killer. Edite-as para informar os registros individuais.")
159: if (!state.started) item { EmptyStateCard("Inicie o campeonato para acompanhar as estatísticas individuais.") }
163: if (standing.position == 1) Text(if (finished) "Killer do Campeonato" else "Líder Killer • parcial", color = AppChampion)
164: Text("${standing.position}º • ${standing.player.name}", style = MaterialTheme.typography.titleMedium)
165: Text("${Engine.teamLabel(standing.team, state.teams)} • Deck: ${standing.player.deck.ifBlank { "Não informado" }}")
166: Text("${standing.kills} kills • ${standing.deaths} mortes • saldo ${standing.balance}")
174: if (error != null) AlertDialog(onDismissRequest = { error = null }, title = { Text("Confira os dados") }, text = { Text(error!!) }, confirmButton = { TextButton(onClick = { error = null }) { Text("Entendi") } })
175: requestEdit?.let { index -> AlertDialog(onDismissRequest = { requestEdit = null }, title = { Text("Corrigir resultado?") },
176: text = { Text("Ao confirmar a correção, a classificação e o Ranking Killer serão recalculados. Os próximos confrontos já sorteados serão mantidos.") },
177: confirmButton = { TextButton(onClick = { editMatch = index; requestEdit = null }) { Text("Corrigir") } },
178: dismissButton = { TextButton(onClick = { requestEdit = null }) { Text("Cancelar") } }) }
184: if (newChampionship) AlertDialog(onDismissRequest = { newChampionship = false }, title = { Text("Criar outro campeonato?") },
185: text = { Text("O campeonato atual será substituído. Uma cópia dos dados atuais será mantida no armazenamento local; a consulta ao histórico ainda não está disponível.") },
189: }) { Text("Criar novo") } }, dismissButton = { TextButton(onClick = { newChampionship = false }) { Text("Cancelar") } })
198: OutlinedTextField(name, onNameChange, label = { Text("Nome do campeonato") }, enabled = !state.started, singleLine = true, modifier = Modifier.fillMaxWidth())
199: OutlinedTextField(rounds, { onRoundsChange(it.filter(Char::isDigit).take(2)) }, label = { Text("Rodadas (1 a $max)") }, enabled = !state.started, singleLine = true, modifier = Modifier.fillMaxWidth())
200: if (state.started) Text("Rodadas bloqueadas após o início: ${state.rounds}.")
203: if (name.isBlank()) error = "Informe o nome do campeonato."
204: else if (count == null || count !in 1..max) error = "Escolha de 1 a $max rodadas para a quantidade atual de participantes."
206: }) { Text(if (state.created) "Salvar configuração" else "Criar campeonato") }
216: OutlinedTextField(player.name, { onChange(player.copy(name = it)) }, label = { Text("Nome do participante") }, enabled = enabled, singleLine = true, modifier = Modifier.fillMaxWidth())
217: OutlinedTextField(player.deck, { onChange(player.copy(deck = it)) }, label = { Text("Deck (obrigatório)") }, enabled = enabled, singleLine = true, modifier = Modifier.fillMaxWidth())
218: if (enabled) TextButton(onClick = onRemove) { Text("Remover participante") }
229: Text("Rodada $round • ${if (completed) "Finalizada" else "Em andamento"} ${if (expanded) "▴" else "▾"}",
238: Text("$a × $b", style = MaterialTheme.typography.titleMedium)
239: Text("Placar: ${game.lossesB} × ${game.lossesA}", style = MaterialTheme.typography.titleLarge)
240: Text(when (official) { 0 -> "Vitória: $a"; 1 -> "Vitória: $b"; 2 -> "Empate"; else -> "Resultado pendente" }, color = when (official) { 0, 1 -> AppWin; 2 -> AppChampion; else -> MaterialTheme.colorScheme.onSurface })
241: if (official in 0..1) Text("Derrota: ${if (official == 0) b else a}", color = AppLoss)
242: game.eliminations.forEach { event -> Text("${state.players.first { it.id == event.killerId }.name} eliminou ${state.players.first { it.id == event.victimId }.name}") }
243: if (game.legacyScore) Text("Placar antigo: autoria das eliminações não informada.")
244: OutlinedButton(onClick = { onEdit(game) }) { Text(if (game.result == -1) "Registrar eliminações e resultado" else "Corrigir resultado") }
264: AlertDialog(onDismissRequest = { close() }, title = { Text(if (match.result == -1) "Registrar partida" else "Corrigir partida") }, text = {
267: Text("Placar: ${draft.lossesB} × ${draft.lossesA}", style = MaterialTheme.typography.titleLarge)
268: Text(when (result) { 0 -> "Vitória: ${Engine.teamLabel(match.a, state.teams)}"; 1 -> "Vitória: ${Engine.teamLabel(match.b, state.teams)}"; else -> "Empate" }, color = if (result == 2) AppChampion else AppWin)
269: Text("Para cada jogador, informe quem o eliminou ou marque que não foi eliminado. O placar é calculado pelos registros.")
270: if (match.legacyScore) Text("Ao salvar, os registros abaixo substituirão o placar antigo.")
278: Text("Deck: ${victim.deck.ifBlank { "Não informado" }}")
281: Text("Não foi eliminado", Modifier.padding(top = 12.dp))
288: Text("Eliminado por ${killer.name}", Modifier.padding(top = 12.dp))
294: }, confirmButton = { TextButton(onClick = { onConfirm(events) }) { Text(if (match.result == -1) "Confirmar resultado" else "Confirmar correção") } }, dismissButton = { TextButton(onClick = { close() }) { Text("Cancelar") } })
295: if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Descartar alterações?") }, text = { Text("Os registros ainda não confirmados serão descartados.") }, confirmButton = { TextButton(onClick = onDismiss) { Text("Descartar") } }, dismissButton = { TextButton(onClick = { discard = false }) { Text("Continuar editando") } })
304: appendLine("🏆 ${state.name}"); appendLine("Resultado final — melhores colocados"); appendLine()
306: appendLine("${listOf("🥇", "🥈", "🥉")[index]} ${index + 1}º: ${Engine.teamLabel(s.team, state.teams)} — ${String.format(Locale.forLanguageTag("pt-BR"), "%.1f", s.points)} ponto(s)")
309: try { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Compartilhar resultado")) }
311: }, modifier = Modifier.fillMaxWidth()) { Text("Compartilhar melhores colocados") }
312: if (error) Text("Nenhum aplicativo disponível para compartilhar.", color = MaterialTheme.colorScheme.error)
```

### app/src/main/java/com/campeonato/duplas/ui/screens/Dc500Screen.kt ? 14 linhas candidatas

```text
18: @SuppressLint("SetJavaScriptEnabled")
31: if (uri.scheme != "https" || uri.host != "appassets.androidplatform.net")
32: return WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), "Blocked".byteInputStream())
34: if (path.contains("..") || !path.startsWith("dc500/"))
35: return WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", emptyMap(), "Not found".byteInputStream())
38: "js" -> "text/javascript"
39: "css" -> "text/css"
40: "png" -> "image/png"
41: else -> "text/html"
43: WebResourceResponse(mime, "UTF-8", context.assets.open(path))
45: WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", emptyMap(), "Not found".byteInputStream())
49: if (request.url.host == "appassets.androidplatform.net") return false
50: if (request.url.scheme == "https") context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
54: loadUrl("https://appassets.androidplatform.net/dc500/dc500.html")
```

### app/src/main/java/com/campeonato/duplas/ui/screens/ModalityScreen.kt ? 10 linhas candidatas

```text
14: var mode by remember { mutableStateOf(prefs.getString("active_modality",null)) }
16: Row { TextButton(onClick={mode=null}){Text("Selecionar modalidade")} }
18: "dc500" -> Dc500App()
19: "cedh" -> CedhApp(remember { CedhRepository(prefs) })
20: "duplas" -> ChampionshipApp(remember { ChampionshipRepository(prefs) })
22: Image(painterResource(R.drawable.championship_logo),contentDescription="Logo Championship Manager",modifier=Modifier.size(112.dp))
23: Text("Championship Manager",style=MaterialTheme.typography.headlineSmall)
24: Text("Criar ou continuar campeonato")
25: listOf("duplas" to "Campeonato de Duplas","cedh" to "cEDH Multiplayer (4 jogadores)","dc500" to "Duel Commander 500 (1?1)").forEach { (key,label) ->
26: Button(onClick={prefs.edit().putString("active_modality",key).apply();mode=key}){Text(label)}
```

### web/app.js ? 36 linhas candidatas

```text
1: import {initial,draw,pairs,confirm,ranking,killers,result} from './engine.js';
2: const app=document.querySelector('#app'),nav=document.querySelector('nav'),status=document.querySelector('#status');let state,db,tab=0,busy=false;
3: const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
5: const button=(text,action,disabled=false)=>`<button type="button" data-action="${action}" ${disabled?'disabled':''}>${text}</button>`;
6: function transaction(mode,action){return new Promise((resolve,reject)=>{const tx=db.transaction('data',mode);let value;try{value=action(tx.objectStore('data'));}catch(e){reject(e);return;}tx.oncomplete=()=>resolve(value?.result);tx.onerror=()=>reject(tx.error);tx.onabort=()=>reject(tx.error||Error('Gravação cancelada'));});}
7: async function save(next){const previous=state;state=next;try{await transaction('readwrite',store=>store.put(next,'current'));status.textContent='Salvo neste aparelho';}catch(error){if(state===next)state=previous;throw error;}}
8: function render(){nav.innerHTML=['Cadastro','Duplas','Confrontos','Classificação','Killer'].map((t,i)=>`<button data-tab="${i}" aria-current="${i===tab}">${t}</button>`).join('');
9: if(tab===0){app.innerHTML=`<h2>Seu campeonato</h2><section><label>Nome<input id="name" value="${esc(state.name)}" ${state.started?'disabled':''}></label><label>Rodadas<input id="rounds" type="number" min="1" max="${state.players.length/2-1}" value="${state.rounds}" ${state.started?'disabled':''}></label><p class="muted">Padrão: 12 jogadores, 6 duplas e 4 rodadas. Nome e deck são obrigatórios. Editar participantes desfaz o sorteio.</p></section><h2>Participantes · ${state.players.length}</h2><div class="grid">${state.players.map(p=>`<article><label>Nome<input data-player="${p.id}" data-field="name" value="${esc(p.name)}" ${state.started?'disabled':''}></label><label>Deck<input data-player="${p.id}" data-field="deck" value="${esc(p.deck)}" ${state.started?'disabled':''}></label>${button('Remover',`remove:${p.id}`,state.started)}</article>`).join('')}</div><div class="row">${button('Adicionar participante','add',state.started||state.players.length>=24)}${button('Sortear duplas','draw',state.started)}</div><section><h3>Seus dados</h3><p>Armazenados somente neste navegador. Exporte um backup antes de limpar dados ou trocar de aparelho.</p>${button('Exportar backup','export')} <label>Restaurar backup<input id="import" type="file" accept="application/json,.json"></label>${button('Novo campeonato','new')}</section>`;}
10: if(tab===1)app.innerHTML=`<h2>Duplas sorteadas</h2>${state.teams.length?'':'<p>Cadastre os participantes e sorteie as duplas.</p>'}<div class="grid">${state.teams.map((t,i)=>`<article><h3>Dupla ${i+1}</h3>${t.map(id=>`<p>${esc(player(id).name)}<br><span class="muted">${esc(player(id).deck)}</span></p>`).join('')}</article>`).join('')}</div>${button('Refazer sorteio','draw',state.started||!state.teams.length)} ${button(state.started?'Campeonato iniciado':'Confirmar duplas e iniciar','start',state.started||!state.teams.length)}`;
11: if(tab===2){const rounds=[...new Set(state.matches.map(m=>m.round))],current=Math.max(0,...rounds);app.innerHTML=`<h2>Confrontos</h2><p>Rodada ${current} de ${state.rounds}. Vitória em 2×0 ou 2×1; 1×0 é empate. 2×2 também é empate.</p>${button('Sortear próxima rodada','next',!state.started||current>=state.rounds||state.matches.some(m=>m.events===null))}${rounds.map(r=>`<details ${r===current?'open':''}><summary>Rodada ${r} · ${state.matches.filter(m=>m.round===r).every(m=>m.events!==null)?'Finalizada':'Em andamento'}</summary>${state.matches.map((m,i)=>m.round===r?matchCard(m,i):'').join('')}</details>`).join('')}`;}
12: if(tab===3)app.innerHTML=`<h2>Classificação geral</h2><p class="muted">Pontos → vitórias → menos mortes → kills → confronto direto → Buchholz.</p>${ranking(state).map((r,i)=>`<article><h3 class="${i===0?'gold':''}">${i+1}º · ${esc(team(r.team))}</h3><p><strong>${r.points} pontos</strong> · ${r.wins}V / ${r.draws}E / ${r.losses}D</p><p>Pessoais: ${r.kills} kills · ${r.deaths} mortes<br>Buchholz: ${r.buchholz}</p></article>`).join('')}${button('Compartilhar pódio','share',!state.matches.length||Math.max(...state.matches.map(m=>m.round))!==state.rounds||state.matches.some(m=>m.events===null))}`;
13: if(tab===4)app.innerHTML=`<h2>Ranking Killer</h2><p class="muted">Mais kills, menos mortes e saldo. Empates compartilham a posição.</p>${state.started?killers(state).map(r=>`<article><h3 class="${r.position===1?'gold':''}">${r.position}º · ${esc(r.name)}</h3>${r.position===1?'<span class="badge">Killer do campeonato · classificação atual</span>':''}<p>${esc(team(r.team))}<br>Deck: ${esc(r.deck)}</p><p>${r.kills} kills · ${r.deaths} mortes · saldo ${r.kills-r.deaths}</p></article>`).join(''):'<p>Inicie o campeonato para acompanhar o ranking.</p>'}`;
15: function matchCard(m,i){const a=(m.events||[]).filter(e=>state.teams[m.b].includes(e.victim)).length,b=(m.events||[]).length-a,r=result(a,b);return `<article><h3>${esc(team(m.a))} × ${esc(team(m.b))}</h3><p>${m.events===null?'Resultado pendente':`<strong>${a} × ${b}</strong> · <span class="${r===2?'gold':'win'}">${r===2?'Empate':'Vitória: '+esc(team(r===0?m.a:m.b))}</span>`}</p>${(m.events||[]).map(e=>`<p>${esc(player(e.killer).name)} eliminou ${esc(player(e.victim).name)}</p>`).join('')}${button(m.events===null?'Registrar resultado':'Corrigir resultado',`edit:${i}`)}</article>`;}
16: function edit(index){const m=state.matches[index];if(m.events!==null&&!window.confirm('Corrigir este resultado? Os rankings serão recalculados, mantendo os confrontos já sorteados.'))return;
17: app.innerHTML=`<h2>Registrar eliminações</h2><p>Selecione quem eliminou cada jogador. O placar será calculado automaticamente.</p><form id="match" data-index="${index}">${[m.a,m.b].flatMap(t=>state.teams[t].map(id=>`<section><label>${esc(player(id).name)} · ${esc(player(id).deck)}<select data-victim="${id}"><option value="">Não foi eliminado</option>${state.teams[t===m.a?m.b:m.a].map(k=>`<option value="${k}" ${(m.events||[]).some(e=>e.victim===id&&e.killer===k)?'selected':''}>Eliminado por ${esc(player(k).name)}</option>`).join('')}</select></label></section>`)).join('')}<p id="preview" aria-live="polite"></p><button type="submit">Confirmar resultado</button> ${button('Cancelar','cancel')}</form>`;nav.querySelectorAll('button').forEach(b=>b.disabled=true);preview(index);}
18: const events=()=>Array.from(app.querySelectorAll('[data-victim]')).filter(x=>x.value!=='').map(x=>({killer:Number(x.value),victim:Number(x.dataset.victim)}));
19: function preview(index){const m=state.matches[index],e=events(),a=e.filter(x=>state.teams[m.b].includes(x.victim)).length,b=e.length-a,r=result(a,b);document.querySelector('#preview').textContent=`Placar: ${a} × ${b} — ${r===2?'Empate':'Vitória: '+team(r===0?m.a:m.b)}`;}
21: app.addEventListener('change',async e=>{try{const el=e.target;if(el.dataset.player!==undefined){if(state.started)throw Error('Cadastro bloqueado.');await save({...state,players:state.players.map(p=>p.id===Number(el.dataset.player)?{...p,[el.dataset.field]:el.value}:p),teams:[]});}
22: if(el.id==='name'||el.id==='rounds'){if(state.started)throw Error('Configuração bloqueada.');const name=document.querySelector('#name').value.trim(),rounds=Number(document.querySelector('#rounds').value);if(!name||!Number.isInteger(rounds)||rounds<1||rounds>state.players.length/2-1)throw Error('Informe um nome e uma quantidade válida de rodadas.');await save({...state,name,rounds});}
23: if(el.dataset.victim!==undefined)preview(Number(document.querySelector('#match').dataset.index));
24: if(el.id==='import'&&el.files[0]){const next=JSON.parse(await el.files[0].text());validate(next);if(window.confirm('Substituir os dados atuais pelo backup?')){await save(next);render();}}
26: app.addEventListener('submit',async e=>{e.preventDefault();try{const index=Number(e.target.dataset.index);await save(confirm(state,index,events()));render();}catch(err){alert(err.message);}});
27: app.onclick=async e=>{const action=e.target.closest('[data-action]')?.dataset.action;if(!action)return;try{
28: if(action==='cancel'){if(window.confirm('Descartar alterações não confirmadas?'))render();return;}
29: if(action.startsWith('edit:')){edit(Number(action.split(':')[1]));return;}
30: if(action==='draw'){await save({...state,teams:draw(state.players)});tab=1;}
31: if(action==='start'){if(state.rounds<1||state.rounds>state.teams.length-1)throw Error('Ajuste a quantidade de rodadas no cadastro.');if(window.confirm('Confirmar duplas e iniciar? Participantes e rodadas serão bloqueados.')){await save({...state,started:true});tab=2;}}
32: if(action==='next')await save({...state,matches:[...state.matches,...pairs(state)]});
33: if(action==='add')await save({...state,players:[...state.players,{id:Math.max(-1,...state.players.map(p=>p.id))+1,name:'',deck:''}],teams:[]});
34: if(action.startsWith('remove:'))await save({...state,players:state.players.filter(p=>p.id!==Number(action.split(':')[1])),teams:[]});
35: if(action==='new'&&window.confirm('Exportou seu backup? Substituir o campeonato atual?'))await save(initial());
36: if(action==='export'){const url=URL.createObjectURL(new Blob([JSON.stringify(state,null,2)],{type:'application/json'}));const a=document.createElement('a');a.href=url;a.download='camp-d-house-backup.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),10000);}
37: if(action==='share'){const text=state.name+'\n'+ranking(state).slice(0,3).map((r,i)=>`${i+1}º ${team(r.team)} — ${r.points} pontos`).join('\n');if(navigator.share)await navigator.share({text});else window.prompt('Copie para enviar pelo WhatsApp:',text);}
38: render();}catch(err){if(err.name!=='AbortError')alert(err.message);}};
39: function validate(s){if(!s||typeof s.name!=='string'||!Array.isArray(s.players)||!Array.isArray(s.teams)||!Array.isArray(s.matches)||!Number.isInteger(s.rounds)||s.rounds<1||s.rounds>11)throw Error('Backup inválido.');if(new Set(s.players.map(p=>p.id)).size!==s.players.length||s.players.some(p=>!Number.isInteger(p.id)||typeof p.name!=='string'||typeof p.deck!=='string'))throw Error('Participantes inválidos.');const ids=s.players.map(p=>p.id),used=s.teams.flat();if(s.teams.some(t=>!Array.isArray(t)||t.length!==2)||used.some(id=>!ids.includes(id))||new Set(used).size!==used.length)throw Error('Duplas inválidas.');const keys=s.matches.map(m=>[m.a,m.b].sort((a,b)=>a-b).join(':'));if(new Set(keys).size!==keys.length)throw Error('Confrontos repetidos no backup.');for(const r of new Set(s.matches.map(m=>m.round))){const games=s.matches.filter(m=>m.round===r);if(games.length!==s.teams.length/2||new Set(games.flatMap(m=>[m.a,m.b])).size!==games.length*2)throw Error('Rodada inválida no backup.');}for(let i=0;i<s.matches.length;i++){const m=s.matches[i];if(!Number.isInteger(m.round)||m.round<1||m.round>s.rounds||!s.teams[m.a]||!s.teams[m.b]||m.a===m.b)throw Error('Confronto inválido.');if(m.events!==null)confirm(s,i,m.events);}if(s.started&&(used.length!==ids.length||s.teams.length%2||s.rounds>s.teams.length-1))throw Error('Configuração inválida.');}
40: try{db=await new Promise((resolve,reject)=>{const r=indexedDB.open('camp-d-house',1);r.onupgradeneeded=()=>r.result.createObjectStore('data');r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error);r.onblocked=()=>reject(Error('Feche outras abas para abrir o armazenamento.'));});state=await transaction('readonly',s=>s.get('current'))||initial();validate(state);render();status.textContent='Dados locais · pronto';if('serviceWorker'in navigator){await navigator.serviceWorker.register('./sw.js');await navigator.serviceWorker.ready;status.textContent='Pronto para uso offline';}}catch(e){status.textContent='Não foi possível preparar o aplicativo';app.textContent=e.message+' — Os dados existentes não foram apagados.';}
```

### web/bootstrap.js ? 8 linhas candidatas

```text
1: const selector=document.createElement('section');
2: selector.innerHTML='<label>Modalidade<select id="modality"><option value="duplas">Campeonato de Duplas</option><option value="cedh">cEDH Multiplayer (4 jogadores)</option><option value="dc500">Duel Commander 500 (1?1)</option></select></label>';
3: document.querySelector('header').append(selector);
4: const selected=document.querySelector('#modality');
5: const requested=new URL(location.href).searchParams.get('mode');
6: const mode=['cedh','dc500'].includes(requested)?requested:'duplas';
8: selected.onchange=()=>{const url=new URL(location.href);url.searchParams.set('mode',selected.value);location.href=url.href;};
9: await import(mode==='dc500'?'./dc500-app.js':mode==='cedh'?'./cedh-app.js':'./app.js');
```

### web/cedh-app.js ? 31 linhas candidatas

```text
1: import * as E from './cedh-engine.js';
2: const app=document.querySelector('#app'),nav=document.querySelector('nav'),status=document.querySelector('#status');
4: const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
6: const button=(text,action,disabled=false)=>`<button type="button" data-action="${action}" ${disabled?'disabled':''}>${text}</button>`;
7: const field=(label,id,value,disabled=false,type='text')=>`<label>${label}<input id="${id}" type="${type}" value="${esc(value)}" ${disabled?'disabled':''}></label>`;
8: function tx(mode,fn){return new Promise((resolve,reject)=>{const t=db.transaction('data',mode);let r;try{r=fn(t.objectStore('data'));}catch(e){t.abort();reject(e);return;}t.oncomplete=()=>resolve(r?.result);t.onerror=()=>reject(t.error);t.onabort=()=>reject(t.error||Error('Gravação cancelada'));});}
9: async function save(next){E.validate(next);const operation=pending.then(()=>tx('readwrite',store=>store.put(next,'cedh_current')));pending=operation.catch(()=>{});await operation;state=next;status.textContent='cEDH salvo neste aparelho';}
11: nav.innerHTML=['Cadastro','Mesas e histórico','Classificação'].map((label,i)=>`<button data-tab="${i}" aria-current="${tab===i}">${label}</button>`).join('');
13: if(tab===0)app.innerHTML=`<h2>cEDH Multiplayer</h2><section>${field('Nome','name',state.name,locked)}${[['rounds','Rodadas'],['minutes','Tempo (minutos)'],['extra','Tempo adicional (minutos)'],['win','Pontos por vitória'],['draw','Pontos por empate'],['loss','Pontos por derrota'],['finalMinutes','Tempo da final (minutos)'],['finalExtra','Adicional da final (minutos)']].map(([key,label])=>field(label,'config-'+key,c[key],locked,'number')).join('')}<label><input id="top4" type="checkbox" ${c.top4?'checked':''} ${locked?'disabled':''}>Final Top 4</label><label><input id="agreement" type="checkbox" ${c.agreement?'checked':''} ${locked?'disabled':''}>Permitir empate por acordo</label><p>Pontos → vitórias → OMW% (adversários únicos). Final exige um vencedor; prossiga a mesa até uma vitória.</p></section><h2>Participantes · ${state.players.length}</h2><div class="grid">${state.players.map(p=>`<article>${[['name','Jogador'],['commanders','Comandante(s)'],['deck','Deck'],['moxfield','Moxfield (opcional)']].map(([key,label])=>`<label>${label}<input data-player="${esc(p.id)}" data-field="${key}" value="${esc(p[key])}" ${locked?'disabled':''}></label>`).join('')}${button('Remover','remove:'+p.id,locked)}</article>`).join('')}</div>${button('Adicionar jogador','add',locked)}<section>${button('Exportar backup','export')}<label>Restaurar backup<input id="import" type="file" accept=".json,application/json"></label>${button('Novo campeonato cEDH','new')} ${button('Campeonatos arquivados','archives')}<div id="archives"></div></section>`;
14: if(tab===1){const round=Math.max(0,...state.tables.map(t=>t.round)),complete=state.tables.every(t=>t.result),canNext=complete&&(round<c.rounds||c.top4&&round===c.rounds);app.innerHTML=`<h2>Mesas</h2>${button('Prévia da próxima rodada','preview',!canNext)}${preview?`<section><h3>Prévia — confirme para salvar</h3>${preview.map((t,i)=>`<p>Mesa ${i+1}: ${t.seats.map(id=>esc(player(id).name)).join(' → ')}</p>`).join('')}${button('Confirmar sorteio','confirm')} ${button('Cancelar','cancel-preview')}</section>`:''}${[...new Set(state.tables.map(t=>t.round))].reverse().map(r=>`<details ${r===round?'open':''}><summary>${r>c.rounds?'Final Top 4':'Rodada '+r}</summary><section><p data-clock="${r}" role="timer"></p><p data-warning="${r}" role="status"></p>${button('Iniciar / retomar','clock:start:'+r)} ${button('Pausar','clock:pause:'+r)} ${button('Reiniciar','clock:reset:'+r)}</section>${state.tables.map((t,i)=>t.round===r?`<article><h3>Mesa ${state.tables.filter(x=>x.round===r).indexOf(t)+1}</h3>${t.seats.map((id,pos)=>`<p>${pos+1}. ${esc(player(id).name)} · ${esc(player(id).commanders)}<br>${esc(player(id).deck)}</p>`).join('')}<p>${t.result?(t.result.winner?'Vitória: '+esc(player(t.result.winner).name):esc(t.result.outcome)):'Pendente'}</p>${t.result?`<p>${t.result.duration}s · ${esc(t.result.notes)}</p>`:''}${button(t.result?'Corrigir resultado':'Registrar resultado','edit:'+i)}</article>`:'').join('')}</details>`).join('')}`;tick();}
15: if(tab===2){const champion=E.champion(state);app.innerHTML=`<h2>Classificação individual</h2>${champion?`<h3 class="gold">Campeão: ${esc(player(champion).name)}</h3>`:''}<p>Pontos → vitórias → OMW% (sem duplicar adversários). ${c.top4?'Classificação anterior à final.':''}</p>${E.ranking(state).map((r,i)=>`<article><h3>${i+1}. ${esc(player(r.id).name)}</h3><p>${esc(player(r.id).commanders)} · ${esc(player(r.id).deck)}</p><p>${r.points} pts · ${r.wins}V ${r.draws}E ${r.losses}D · ${r.games} partidas<br>Vitórias ${(r.rate*100).toFixed(1)}% · OMW ${(r.omw*100).toFixed(1)}%</p></article>`).join('')}`;}
17: function tick(){if(!state||editing!==null)return;for(const el of app.querySelectorAll('[data-clock]')){const round=Number(el.dataset.clock),clock=state.clocks[round]||{elapsed:0,since:null},isFinal=state.tables.find(t=>t.round===round)?.final,extra=isFinal?state.config.finalExtra:state.config.extra,remaining=(isFinal?state.config.finalMinutes:state.config.minutes)*60-Math.floor(E.elapsed(clock)/1000);el.textContent=remaining>0?`Tempo restante: ${Math.floor(remaining/60)}:${String(remaining%60).padStart(2,'0')}`:`Adicional restante: ${Math.max(0,extra*60+remaining)} s`;app.querySelector(`[data-warning="${round}"]`).textContent=remaining>0?'':remaining> -extra*60?'Tempo encerrado. O jogador ativo pode concluir seu turno.':(isFinal?'Final: continue até haver um vencedor.':'Limite adicional encerrado. Sem vencedor, registre empate por tempo.');}}
19: function edit(index){editing=index;const t=state.tables[index],r=t.result||{outcome:'TIME_DRAW',winner:null,eliminated:{},penalties:{},duration:0,notes:''};app.innerHTML=`<h2>Resultado da mesa</h2><form id="result"><label>Resultado<select id="outcome"><option value="TIME_DRAW">Empate por tempo</option>${state.config.agreement?'<option value="AGREEMENT_DRAW">Empate por acordo</option>':''}<option value="ADMINISTRATIVE">Administrativo</option>${t.seats.map(id=>`<option value="win:${esc(id)}">Vitória: ${esc(player(id).name)}</option>`).join('')}</select></label>${t.seats.map(id=>`<section><h3>${esc(player(id).name)}</h3><label><input type="checkbox" data-eliminated="${esc(id)}" ${id in r.eliminated?'checked':''}>Eliminado</label><label>Causa (opcional)<input data-cause="${esc(id)}" value="${esc(r.eliminated[id]||'')}"></label><label>Ajuste administrativo de pontos<input type="number" data-penalty="${esc(id)}" value="${r.penalties[id]||0}"></label></section>`).join('')}${field('Duração (segundos)','duration',r.duration,false,'number')}${field('Observações','notes',r.notes)}<p>Eliminações não determinam o vencedor. Empates pontuam todos, inclusive eliminados.</p>${t.final?'<p>A final exige vencedor. Continue a mesa conforme o procedimento anunciado.</p>':''}<button type="submit">Salvar resultado</button> ${button('Cancelar','cancel-edit')}</form>`;document.querySelector('#outcome').value=r.winner?'win:'+r.winner:r.outcome;}
21: app.addEventListener('change',async e=>{if(busy)return;busy=true;try{const el=e.target;if(el.dataset.player){if(state.tables.length)throw Error('Cadastro bloqueado');await save({...state,players:state.players.map(p=>p.id===el.dataset.player?{...p,[el.dataset.field]:el.value}:p)});}
22: if(el.id==='name'||el.id.startsWith('config-')||['top4','agreement'].includes(el.id)){if(state.tables.length)throw Error('Configuração bloqueada');if(el.id==='name')await save({...state,name:el.value});else{const key=el.id.replace('config-','');await save({...state,config:{...state.config,[key]:el.type==='checkbox'?el.checked:Number(el.value)}});}}
23: if(el.id==='import'&&el.files[0]){const next=E.validate(JSON.parse(await el.files[0].text()));if(confirm('Arquivar os dados atuais e restaurar backup?')){await tx('readwrite',store=>store.put(state,'cedh_archive_'+state.id));await save(next);render();}}
25: app.addEventListener('submit',async e=>{e.preventDefault();if(busy||editing===null)return;busy=true;try{const choice=document.querySelector('#outcome').value,winner=choice.startsWith('win:')?choice.slice(4):null,eliminated={},penalties={};for(const el of app.querySelectorAll('[data-eliminated]'))if(el.checked)eliminated[el.dataset.eliminated]=[...app.querySelectorAll('[data-cause]')].find(x=>x.dataset.cause===el.dataset.eliminated).value;for(const el of app.querySelectorAll('[data-penalty]'))penalties[el.dataset.penalty]=Number(el.value);await save(E.result(state,editing,{outcome:winner?'WIN':choice,winner,eliminated,penalties,duration:Number(document.querySelector('#duration').value),notes:document.querySelector('#notes').value}));editing=null;render();}catch(e){alert(e.message);}finally{busy=false;}});
26: app.onclick=async e=>{await pending;const action=e.target.closest('[data-action]')?.dataset.action;if(!action||busy)return;busy=true;try{
27: if(action==='archives'){const all=await tx('readonly',store=>store.getAll());document.querySelector('#archives').innerHTML=all.filter(x=>x?.version===1&&x.id!==state.id).map(x=>`<p>${button(esc(x.name),'restore:'+esc(x.id))}</p>`).join('')||'<p>Nenhum campeonato arquivado.</p>';return;}
28: if(action.startsWith('restore:')){const old=await tx('readonly',store=>store.get('cedh_archive_'+action.slice(8)));if(old){await tx('readwrite',store=>store.put(state,'cedh_archive_'+state.id));await save(E.validate(old));}}
29: if(action==='add')await save({...state,players:[...state.players,{id:crypto.randomUUID(),name:'',deck:'',commanders:'',moxfield:''}]});
30: if(action.startsWith('remove:')){if(state.tables.length)throw Error('Cadastro bloqueado');await save({...state,players:state.players.filter(p=>p.id!==action.slice(7))});}
31: if(action==='preview'){const round=Math.max(0,...state.tables.map(t=>t.round));preview=round<state.config.rounds?E.pair(state):[E.finalTable(state)];}
32: if(action==='cancel-preview')preview=null;
33: if(action==='confirm'&&preview){await save({...state,tables:[...state.tables,...preview]});preview=null;}
34: if(action.startsWith('edit:')){edit(Number(action.slice(5)));return;}
35: if(action==='cancel-edit')editing=null;
36: if(action.startsWith('clock:')){const [,kind,r]=action.split(':');if(kind!=='reset'||confirm('Reiniciar e zerar o tempo desta rodada?'))await save(E.timer(state,Number(r),kind));}
37: if(action==='new'&&confirm('Arquivar este campeonato e criar outro?')){await tx('readwrite',store=>store.put(state,'cedh_archive_'+state.id));await save(E.initial());preview=null;}
38: if(action==='export'){const url=URL.createObjectURL(new Blob([JSON.stringify(state)],{type:'application/json'})),a=document.createElement('a');a.href=url;a.download='cedh-backup.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
40: try{db=await new Promise((resolve,reject)=>{const r=indexedDB.open('camp-d-house',1);r.onupgradeneeded=()=>r.result.createObjectStore('data');r.onsuccess=()=>resolve(r.result);r.onerror=()=>reject(r.error);r.onblocked=()=>reject(Error('Feche outras abas'));});state=E.validate(await tx('readonly',s=>s.get('cedh_current'))||E.initial());render();status.textContent='cEDH · dados locais';if('serviceWorker'in navigator){await navigator.serviceWorker.register('./sw.js');await navigator.serviceWorker.ready;status.textContent='cEDH pronto para uso offline';}}catch(e){app.textContent='Dados preservados. Não foi possível abrir: '+e.message;}
```

### web/cedh-engine.js ? 10 linhas candidatas

```text
1: export const initial=()=>({version:1,id:crypto.randomUUID(),name:'cEDH Multiplayer',config:{rounds:4,minutes:75,extra:10,win:5,draw:1,loss:0,top4:false,agreement:false,finalMinutes:75,finalExtra:10},players:[],tables:[],clocks:{}});
2: const check=(ok,message='Dados cEDH inválidos')=>{if(!ok)throw Error(message)};
4: check(s?.version===1&&typeof s.id==='string'&&typeof s.name==='string'&&s.name.trim());
5: const c=s.config;c.finalMinutes??=75;c.finalExtra??=10;check(c&&['rounds','minutes','extra','win','draw','loss'].every(k=>Number.isInteger(c[k]))&&c.rounds>0&&c.minutes>0&&c.extra>=0&&c.win>=0&&c.draw>=0&&c.loss>=0&&Number.isInteger(c.finalMinutes)&&c.finalMinutes>0&&Number.isInteger(c.finalExtra)&&c.finalExtra>=0);
7: check(s.players.every(p=>typeof p.id==='string'&&p.id&&['name','deck','commanders','moxfield'].every(k=>typeof p[k]==='string')));
11: for(const t of tables){check(t.seats.length===4);const r=t.result;if(!r)continue;check(['WIN','TIME_DRAW','AGREEMENT_DRAW','ADMINISTRATIVE'].includes(r.outcome));check((r.outcome==='WIN')===(r.winner!==null));check(r.winner===null||t.seats.includes(r.winner));check(r.outcome!=='AGREEMENT_DRAW'||c.agreement);check(!t.final||r.winner!==null);check(Number.isInteger(r.duration)&&r.duration>=0);check(Object.keys(r.eliminated).every(id=>t.seats.includes(id)&&id!==r.winner));check(Object.entries(r.penalties).every(([id,n])=>t.seats.includes(id)&&Number.isInteger(n)));}}
15: export function ranking(s){const done=s.tables.filter(t=>!t.final&&t.result),rows=s.players.map(p=>{const games=done.filter(t=>t.seats.includes(p.id)),wins=games.filter(t=>t.result.winner===p.id).length,draws=games.filter(t=>['TIME_DRAW','AGREEMENT_DRAW'].includes(t.result.outcome)).length,losses=games.length-wins-draws;return {id:p.id,wins,draws,losses,games:games.length,rate:games.length?wins/games.length:0,points:wins*s.config.win+draws*s.config.draw+losses*s.config.loss+games.reduce((n,t)=>n+(t.result.penalties[p.id]||0),0)};});for(const row of rows){const opponents=new Set(done.filter(t=>t.seats.includes(row.id)).flatMap(t=>t.seats).filter(id=>id!==row.id));row.omw=opponents.size?[...opponents].reduce((n,id)=>n+rows.find(r=>r.id===id).rate,0)/opponents.size:0;}return rows.sort((a,b)=>b.points-a.points||b.wins-a.wins||b.omw-a.omw||a.id.localeCompare(b.id));}
17: export function pair(s,rng=Math.random){validate(s);check(s.players.length>=4&&s.players.length%4===0,'Use um múltiplo de quatro participantes');check(s.players.every(p=>p.name.trim()&&p.deck.trim()&&p.commanders.trim()),'Preencha nome, deck e comandantes');check(s.tables.every(t=>t.result)&&!s.tables.some(t=>t.final),'Resultados pendentes ou final já criada');const round=Math.max(0,...s.tables.map(t=>t.round))+1;check(round<=s.config.rounds,'Classificatórias encerradas');const points=Object.fromEntries(ranking(s).map(r=>[r.id,r.points]));
23: export function finalTable(s){validate(s);check(s.config.top4&&s.tables.length&&s.tables.every(t=>t.result)&&!s.tables.some(t=>t.final)&&Math.max(...s.tables.map(t=>t.round))===s.config.rounds,'Finalize as classificatórias');return {round:s.config.rounds+1,seats:ranking(s).slice(0,4).map(r=>r.id),result:null,final:true};}
26: export function timer(s,round,action,now=Date.now()){check(s.tables.some(t=>t.round===round));const old=s.clocks[round]||{elapsed:0,since:null};let clock;if(action==='reset')clock={elapsed:0,since:null};else if(action==='pause')clock={elapsed:elapsed(old,now),since:null};else if(action==='start')clock=old.since===null?{...old,since:now}:old;else throw Error('Ação inválida');return validate({...s,clocks:{...s.clocks,[round]:clock}});}
```

### web/dc500-app.js ? 20 linhas candidatas

```text
1: import * as E from './dc500-engine.js';
2: import * as Store from './dc500-store.js';
3: const app=document.querySelector('#app'),nav=document.querySelector('nav'),status=document.querySelector('#status');
5: const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
6: const field=(label,key,value,type='text',disabled=false)=>`<label>${label}<input name="${key}" type="${type}" ${type==='number'?'step="any"':''} value="${esc(value)}" ${disabled?'disabled':''}></label>`;
7: const btn=(text,action)=>`<button type="button" data-action="${action}">${text}</button>`;
8: const select=(label,key,value,options,disabled=false)=>`<label>${label}<select name="${key}" ${disabled?'disabled':''}>${options.map(([v,t])=>`<option value="${v}" ${String(value)===String(v)?'selected':''}>${t}</option>`).join('')}</select></label>`;
9: const name=id=>s.players.find(p=>p.id===id)?.name||'BYE';
10: function save(next){Store.persist(next);s=next;status.textContent='DC500 salvo neste aparelho';}
11: function render(){editing=null;nav.innerHTML=['Configuração','Participantes e decks','Rodadas e chaveamento','Classificação'].map((x,i)=>`<button data-tab="${i}" aria-current="${tab===i}">${x}</button>`).join('');const c=s.config,locked=s.rounds.length>0;
12: if(tab===0)app.innerHTML=`<h2>Duel Commander 500 · 1×1</h2><p>20 pontos de vida · 100 cartas incluindo comandante(s) · identidade de cores · sem derrota especial por dano de comandante.</p><p><a href="https://www.duelcommander.org/rules/duelcommander_comprehensiverules/" rel="noopener">Regras oficiais</a> · <a href="https://www.duelcommander.org/banlist/" rel="noopener">Banlist oficial</a></p><p>A validação do orçamento é manual e não certifica legalidade do deck. Valor declarado exclui comandante(s). Confira a lista e as regras oficiais com o organizador.</p><form id="config">${field('Nome','name',s.name,'text',locked)}${[['rounds','Rodadas suíças'],['minutes','Minutos por rodada'],['win','Pontos por vitória'],['draw','Pontos por empate'],['loss','Pontos por derrota'],['bye','Pontos por BYE'],['budget','Limite em reais']].map(([k,l])=>field(l,k,c[k],'number',locked)).join('')}${select('Formato','bestOf',c.bestOf,[[1,'Melhor de 1'],[3,'Melhor de 3']],locked)}${select('Eliminatória','top',c.top,[[0,'Somente suíço'],[4,'Top 4'],[8,'Top 8']],locked)}${['intentional','administrative','secondCommander'].map((k,i)=>select(['Permitir empates intencionais','Permitir resultados administrativos','Permitir segundo comandante elegível'][i],k,c[k],[[false,'Não'],[true,'Sim']],locked)).join('')}${field('Desempates em ordem (mw,omw,gw,ogw; separados por vírgula)','ties',c.ties.join(','),'text',locked)}${field('Procedimento de turnos adicionais','extraTurns',c.extraTurns,'text',locked)}${field('Procedimento de desempate eliminatório (definir antes do início)','eliminationProcedure',c.eliminationProcedure,'text',locked)}${locked?'<p>Configuração bloqueada após início.</p>':'<button>Salvar configuração</button>'}</form><section>${btn('Exportar backup','export')}<label>Restaurar backup JSON<input id="import" type="file" accept="application/json,.json"></label><label>Backup JSON (copie ou cole)<textarea id="backup" rows="6"></textarea></label>${btn('Restaurar JSON colado','paste')}${btn('Novo campeonato (arquivar atual)','new')}${btn('Ver arquivos locais','archives')}<div id="archives"></div></section>`;
13: if(tab===1)app.innerHTML=`<h2>Participantes e validação manual</h2><p>Pendente ou inválido não pode iniciar o torneio. Aprovação refere-se somente ao orçamento. Cadastro e orçamento são bloqueados após início.</p>${s.players.map(p=>`<article><form data-player="${p.id}">${field('Jogador','name',p.name,'text',locked)}${field('Comandante principal','commander',p.commander,'text',locked)}${c.secondCommander?field('Segundo comandante (somente quando permitido pelas regras)','second',p.second,'text',locked):''}${field('Deck','deck',p.deck,'text',locked)}${field('Link da decklist','url',p.url,'url',locked)}${field('Valor em reais sem comandantes','value',p.value,'number',locked)}${select('Validação de orçamento pelo organizador','validation',p.validation,[['pending','Pendente'],['approved','Aprovado manualmente'],['invalid','Inválido']],locked)}${locked?'':`<button>Salvar jogador</button>${btn('Remover','remove:'+p.id)}`}</form></article>`).join('')}${locked?'':btn('Adicionar participante','add')}`;
14: if(tab===2)app.innerHTML=`<h2>Rodadas e chaveamento</h2><p>Corrigir o su??o invalida o Top j? gerado. Corrigir uma eliminat?ria refaz as fases seguintes; seus resultados e cron?metros s?o descartados.</p>${btn(s.rounds.length<c.rounds?'Sortear próxima rodada suíça':'Gerar próxima fase do Top','next')}<p>${esc(c.eliminationProcedure)}</p>${s.rounds.map((r,ri)=>`<details ${ri===s.rounds.length-1?'open':''}><summary>${r.elimination?'Top '+r.matches.length*2:'Rodada '+(ri+1)}</summary>${r.repeats?'<p role="alert">Há revanche: não foi encontrada combinação sem repetição.</p>':''}<p data-clock="${ri}" role="timer"></p><p data-warning="${ri}" role="status"></p>${btn('Iniciar / retomar','start:'+ri)} ${btn('Pausar','pause:'+ri)} ${field('Extensão (minutos)','extension-'+ri,5,'number')}${btn('Adicionar extensão','extend:'+ri)}${r.matches.map((m,mi)=>`<article><h3>${esc(name(m.a))} × ${m.b?esc(name(m.b)):'BYE'}</h3><p>${!m.b?`${c.bye} pontos · BYE separado de vitória`:m.result?`${m.result.games.map(g=>g==='a'?'1–0':g==='b'?'0–1':'empate').join(', ')||'Sem jogos'} · ${m.result.winner?'Vitória: '+esc(name(m.result.winner)):'Empate'} · ${esc(m.result.kind)}`:'Pendente'}</p>${m.result?`<p>${esc(m.result.notes)} · ajustes: ${esc(JSON.stringify(m.result.adjustments))}${m.result.concession?' · concessão: '+esc(name(m.result.concession)):''}</p>`:''}${m.b?btn(m.result?'Corrigir resultado':'Registrar resultado',`result:${ri}:${mi}`):''}</article>`).join('')}</details>`).reverse().join('')}`;
15: if(tab===3){const champion=E.champion(s);app.innerHTML=`<h2>Classificação suíça</h2>${champion?`<h3 class="gold">Campeão: ${esc(name(champion))}</h3>`:''}<p>Pontos → ${c.ties.join(' → ').toUpperCase()}. BYEs excluídos dos percentuais. Empate vale 1/3 de vitória nos percentuais. Piso de 33,33% por adversário. Ajustes afetam somente pontos.</p>${E.ranking(s).map((p,i)=>`<article><h3>${i+1}. ${esc(p.name)}</h3><p>${esc(p.commander)} ${esc(p.second)} · ${esc(p.deck)}</p><p>${p.points} pontos · ${p.wins}V ${p.draws}E ${p.losses}D · ${p.matches} confrontos · ${p.byes} BYE(s)<br>Jogos: ${p.gameWins}V ${p.gameDraws}E ${p.gameLosses}D · Vitórias ${(100*p.winRate).toFixed(2)}%<br>${['mw','omw','gw','ogw'].map(k=>`${k.toUpperCase()} ${(100*p[k]).toFixed(2)}%`).join(' · ')}</p></article>`).join('')}`;}tick();
17: function edit(ri,mi){editing=[ri,mi];const m=s.rounds[ri].matches[mi],v=m.result||{games:[],kind:'played',adjustments:{},notes:'',concession:null};app.innerHTML=`<h2>${esc(name(m.a))} × ${esc(name(m.b))}</h2><form id="result">${select('Tipo de resultado','kind',v.kind,[['played','Disputado'],['time','Após concluir turnos adicionais'],...(s.config.intentional?[['intentional','Empate intencional']]:[]),...(s.config.administrative?[['administrative','Administrativo']]:[])])}${Array.from({length:s.config.bestOf},(_,i)=>select('Jogo '+(i+1),'game'+i,v.games[i]||'',[['','Não disputado'],['a','Vitória de '+esc(name(m.a))],['b','Vitória de '+esc(name(m.b))],['draw','Empate']])).join('')}${select('Vencedor administrativo (somente tipo administrativo)','winner',v.winner||'',[['','Empate'],[m.a,esc(name(m.a))],[m.b,esc(name(m.b))]])}${select('Concessão','concession',v.concession||'',[['','Nenhuma'],[m.a,esc(name(m.a))],[m.b,esc(name(m.b))]])}${field('Ajuste de pontos de '+name(m.a)+' (negativo para penalidade)','adjustA',v.adjustments[m.a]||0,'number')}${field('Ajuste de pontos de '+name(m.b),'adjustB',v.adjustments[m.b]||0,'number')}${field('Observações do organizador','notes',v.notes)}<p>Encerrar o cronômetro não encerra o confronto. ${esc(s.config.extraTurns)} ${s.rounds[ri].elimination?esc(s.config.eliminationProcedure):''}</p><button>Salvar resultado</button>${btn('Cancelar','cancel')}</form>`;}
18: const warned=new Set();function tick(){if(!s||editing)return;for(const el of app.querySelectorAll('[data-clock]')){const r=Number(el.dataset.clock),left=E.remaining(s,r);el.textContent=left>0?`Tempo restante: ${Math.floor(left/60000)}:${String(Math.floor(left/1000)%60).padStart(2,'0')}`:'Tempo regulamentar encerrado';app.querySelector(`[data-warning="${r}"]`).textContent=left<=0?s.config.extraTurns:'';if(left<=0&&!warned.has(r)){warned.add(r);navigator.vibrate?.([200,100,200]);}}}
21: app.onsubmit=e=>{e.preventDefault();try{const data=Object.fromEntries(new FormData(e.target));if(e.target.id==='config'){if(s.rounds.length)throw Error('Configuração bloqueada');const config={...s.config};for(const k of ['rounds','minutes','bestOf','win','draw','loss','bye','budget','top'])config[k]=Number(data[k]);for(const k of ['intentional','administrative','secondCommander'])config[k]=data[k]==='true';config.ties=data.ties.split(',').map(x=>x.trim()).filter(Boolean);config.extraTurns=data.extraTurns;config.eliminationProcedure=data.eliminationProcedure;save({...s,name:data.name,config});}else if(e.target.dataset.player){if(s.rounds.length)throw Error('Cadastro bloqueado');save({...s,players:s.players.map(p=>p.id===e.target.dataset.player?{...p,...data,value:Number(data.value)}:p)});}else if(e.target.id==='result'){const [ri,mi]=editing,m=s.rounds[ri].matches[mi];const games=Array.from({length:s.config.bestOf},(_,i)=>data['game'+i]);const firstEmpty=games.indexOf('');if(firstEmpty>=0&&games.slice(firstEmpty).some(Boolean))throw Error('Preencha os jogos em sequência');save(E.setResult(s,ri,mi,{games:games.filter(Boolean),kind:data.kind,winner:data.winner||null,concession:data.concession||null,adjustments:{[m.a]:Number(data.adjustA),[m.b]:Number(data.adjustB)},notes:data.notes}));}render();}catch(err){alert(err.message);}};
22: app.onclick=e=>{const action=e.target.closest('[data-action]')?.dataset.action;if(!action)return;try{const [kind,a,b]=action.split(':');if(kind==='add'){if(s.rounds.length)throw Error('Cadastro bloqueado');save({...s,players:[...s.players,{id:crypto.randomUUID(),name:'',commander:'',second:'',deck:'',url:'',value:0,validation:'pending'}]});}if(kind==='remove'){if(s.rounds.length)throw Error('Cadastro bloqueado');save({...s,players:s.players.filter(p=>p.id!==a)});}if(kind==='next'){const r=s.rounds.length<s.config.rounds?E.pair(s):E.nextTop(s);save({...s,rounds:[...s.rounds,r]});}if(kind==='result'){edit(Number(a),Number(b));return;}if(['start','pause','extend'].includes(kind))save(E.timer(s,Number(a),kind,Date.now(),kind==='extend'?Number(app.querySelector(`[name="extension-${a}"]`).value):0));if(kind==='new'&&confirm('Arquivar o campeonato atual e iniciar outro?')){Store.archive(s);save(E.initial());}if(kind==='paste'){const next=E.decode(document.querySelector('#backup').value);if(confirm('Arquivar atual e restaurar backup?')){Store.archive(s);save(next);}}if(kind==='export'){document.querySelector('#backup').value=E.encode(s);if(location.host==='appassets.androidplatform.net')return;const url=URL.createObjectURL(new Blob([E.encode(s)],{type:'application/json'}));const link=document.createElement('a');link.href=url;link.download='dc500-backup.json';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000);return;}if(kind==='archives'){document.querySelector('#archives').innerHTML=Store.archives().map(x=>`<p>${btn(esc(x.name),'restore:'+x.id)}</p>`).join('')||'Nenhum arquivo';return;}if(kind==='restore'){const old=Store.archives().find(x=>x.id===a);if(old&&confirm('Arquivar atual e restaurar?')){Store.archive(s);save(old);}}render();}catch(err){alert(err.message);}};
23: app.onchange=async e=>{if(e.target.id==='import'&&e.target.files[0])try{const next=E.decode(await e.target.files[0].text());if(confirm('Arquivar atual e restaurar backup DC500?')){Store.archive(s);save(next);render();}}catch(err){alert(err.message);}};
24: try{s=Store.load();render();status.textContent='DC500 · dados locais';if(location.host!=='appassets.androidplatform.net'&&location.protocol!=='file:'&&'serviceWorker'in navigator)navigator.serviceWorker.register('./sw.js').catch(()=>{});}catch(err){app.textContent='Dados preservados. Falha ao abrir DC500: '+err.message;}
```

### web/dc500-engine.js ? 19 linhas candidatas

```text
1: const check=(ok,message='Dados DC500 inválidos')=>{if(!ok)throw Error(message)};
2: export const initial=()=>({version:1,modality:'dc500',id:crypto.randomUUID(),name:'Duel Commander 500',config:{rounds:4,minutes:50,bestOf:3,win:3,draw:1,loss:0,budget:500,top:0,intentional:false,administrative:false,bye:3,secondCommander:false,ties:['mw','omw','gw','ogw'],extraTurns:'Concluir o turno atual e jogar cinco turnos adicionais, conforme o regulamento aplicável.',eliminationProcedure:'Continuar até haver vencedor conforme procedimento anunciado pelo organizador.'},players:[],rounds:[],clocks:{}});
5: check(s?.version===1&&s.modality==='dc500'&&typeof s.id==='string'&&/^[a-zA-Z0-9_-]+$/.test(s.id)&&typeof s.name==='string'&&s.name.trim());const c=s.config;
6: check(c&&['rounds','minutes','win','draw','loss','bye'].every(k=>Number.isInteger(c[k])&&c[k]>=0)&&c.rounds>0&&c.minutes>0&&[1,3].includes(c.bestOf)&&[0,4,8].includes(c.top)&&Number.isFinite(c.budget)&&c.budget>=0,'Configuração inválida');
7: check(['intentional','administrative','secondCommander'].every(k=>typeof c[k]==='boolean')&&typeof c.extraTurns==='string'&&c.extraTurns.trim()&&typeof c.eliminationProcedure==='string'&&c.eliminationProcedure.trim());check(Array.isArray(c.ties)&&new Set(c.ties).size===c.ties.length&&c.ties.every(k=>['mw','omw','gw','ogw'].includes(k)));
8: check(Array.isArray(s.players)&&Array.isArray(s.rounds));const ids=s.players.map(p=>p.id);check(new Set(ids).size===ids.length&&ids.every(id=>typeof id==='string'&&/^[a-zA-Z0-9_-]+$/.test(id)));
9: for(const p of s.players){check(['name','commander','second','deck','url'].every(k=>typeof p[k]==='string')&&Number.isFinite(p.value)&&p.value>=0&&['pending','approved','invalid'].includes(p.validation));check(!p.second||c.secondCommander,'Segundo comandante não permitido');check(p.validation!=='approved'||p.value<=c.budget,'Orçamento aprovado excede o limite');}
10: if(s.rounds.length)check(s.players.length>=2&&s.players.every(p=>p.validation==='approved'&&p.name.trim()&&p.commander.trim()&&p.deck.trim()),'Participantes n?o aprovados');
11: for(let i=0;i<s.rounds.length;i++){const r=s.rounds[i];check(typeof r.elimination==='boolean'&&Array.isArray(r.matches)&&r.matches.length>0);check(i<c.rounds?!r.elimination:r.elimination&&c.top>0);if(i)check(s.rounds[i-1].matches.every(m=>!m.b||m.result),'Rodada anterior pendente');const seats=r.matches.flatMap(idsOf);check(new Set(seats).size===seats.length&&seats.every(id=>ids.includes(id)),'Jogador duplicado ou desconhecido na rodada');if(!r.elimination)check(seats.length===ids.length&&r.matches.filter(m=>!m.b).length===ids.length%2,'Rodada incompleta');else{check(r.matches.every(m=>m.b),'BYE na eliminatória');const expected=i===c.rounds?ranking(s).slice(0,c.top).map(p=>p.id):s.rounds[i-1].matches.map(m=>m.result?.winner);check(expected.length===seats.length&&seats.every(id=>expected.includes(id)),'Chaveamento incompatível');}
12: for(const m of r.matches){check(!!m.a&&m.a!==m.b);if(m.result){check(!!m.b&&Array.isArray(m.result.games)&&m.result.adjustments&&typeof m.result.adjustments==='object'&&typeof m.result.notes==='string'&&typeof m.result.kind==='string');const normalized=makeResult(s,m,m.result,r.elimination);check(normalized.winner===m.result.winner,'Vencedor incompatível com jogos');}}
18: for(const r of s.rounds.filter(r=>!r.elimination))for(const m of r.matches){if(!m.b){map[m.a].points+=s.config.bye;map[m.a].byes++;continue}if(!m.result)continue;const v=m.result;for(const [id,other,side]of [[m.a,m.b,'a'],[m.b,m.a,'b']]){const p=map[id];p.matches++;p.opponents.push(other);const key=v.winner===id?'wins':v.winner?'losses':'draws';p[key]++;p.points+=s.config[{wins:'win',draws:'draw',losses:'loss'}[key]]+(v.adjustments[id]||0);for(const g of v.games){if(g===side)p.gameWins++;else if(g==='draw')p.gameDraws++;else p.gameLosses++;}}}
24: const {games=[],kind='played',winner=null,adjustments={},notes='',concession=null}=input;check(m.b&&Array.isArray(games)&&games.every(g=>['a','b','draw'].includes(g)),'Jogos inválidos');check(['played','time','intentional','administrative'].includes(kind));check(kind!=='intentional'||s.config.intentional,'Empate intencional desativado');check(kind!=='administrative'||s.config.administrative,'Resultado administrativo desativado');const a=games.filter(g=>g==='a').length,b=games.filter(g=>g==='b').length,target=s.config.bestOf===3?2:1;check(games.length<=s.config.bestOf&&a<=target&&b<=target,'Placar inválido');let resolved=winner;
25: if(kind!=='administrative'){resolved=a>b?m.a:b>a?m.b:null;check(kind==='played'?Math.max(a,b)===target&&a!==b:kind==='intentional'?a===0&&b===0&&games.length===0:Math.max(a,b)<target,'Resultado incompatível');if(kind==='intentional')resolved=null;}
26: let wa=0,wb=0;for(let i=0;i<games.length;i++){check(wa<target&&wb<target,'Há jogos após o encerramento do confronto');if(games[i]==='a')wa++;if(games[i]==='b')wb++;}
27: check(resolved===null||idsOf(m).includes(resolved));check(!elimination||resolved!==null,'Eliminatória exige vencedor; conclua o procedimento de desempate');check(concession===null||idsOf(m).includes(concession));check(typeof notes==='string'&&Object.entries(adjustments).every(([id,n])=>idsOf(m).includes(id)&&Number.isInteger(n)),'Ajuste de pontos inválido');const points=Object.fromEntries(idsOf(m).map(id=>[id,s.config[resolved===id?'win':resolved?'loss':'draw']+(adjustments[id]||0)]));return {games,kind,winner:resolved,points,adjustments,notes,concession};
29: export function pair(s,rng=Math.random){validate(s);check(s.rounds.length<s.config.rounds&&s.rounds.every(r=>r.matches.every(m=>!m.b||m.result)),'Conclua a rodada anterior ou inicie o Top');check(s.players.length>=2&&s.players.every(p=>p.name.trim()&&p.commander.trim()&&p.deck.trim()&&p.validation==='approved'),'Preencha os participantes e valide manualmente os orçamentos');let ps=ranking(s);if(!s.rounds.length)for(let i=ps.length-1;i>0;i--){const j=Math.floor(rng()*(i+1));[ps[i],ps[j]]=[ps[j],ps[i]];}const matches=[];if(ps.length%2){const min=Math.min(...ps.map(p=>p.byes));const p=[...ps].reverse().find(p=>p.byes===min);ps=ps.filter(x=>x.id!==p.id);matches.push({a:p.id,b:null,result:null});}
31: let paired=solve(ps,false);if(!paired){visits=0;paired=solve(ps,true);}check(paired,'Não foi possível emparelhar');const round={elimination:false,matches:[...matches,...paired],repeats:paired.some(m=>seen(m.a,m.b))};validate({...s,rounds:[...s.rounds,round]});return round;
33: export function nextTop(s){validate(s);check(s.config.top&&s.rounds.length>=s.config.rounds&&s.rounds.every(r=>r.matches.every(m=>!m.b||m.result)),'Conclua o suíço e configure o Top');const last=s.rounds.at(-1);let ids;if(last.elimination){check(last.matches.every(m=>m.result?.winner),'Vencedor obrigatório');ids=last.matches.map(m=>m.result.winner);check(ids.length>1,'Final concluída');}else{ids=ranking(s).slice(0,s.config.top).map(p=>p.id);check(ids.length===s.config.top,'Participantes insuficientes');ids=ids.length===4?[ids[0],ids[3],ids[1],ids[2]]:[ids[0],ids[7],ids[3],ids[4],ids[1],ids[6],ids[2],ids[5]];}const r={elimination:true,matches:Array.from({length:ids.length/2},(_,i)=>({a:ids[i*2],b:ids[i*2+1],result:null}))};validate({...s,rounds:[...s.rounds,r]});return r;}
42: export function timer(s,r,action,now=Date.now(),minutes=0){check(s.rounds[r]);const old=s.clocks[r]||{elapsed:0,since:null,extension:0};let c;if(action==='start')c=old.since===null?{...old,since:now}:old;else if(action==='pause')c={...old,elapsed:elapsed(old,now),since:null};else{check(action==='extend'&&Number.isFinite(minutes)&&minutes>0,'Extensão inválida');c={...old,extension:old.extension+minutes*60000};}return validate({...s,clocks:{...s.clocks,[r]:c}});}
```

### web/dc500-store.js ? 4 linhas candidatas

```text
1: import * as E from './dc500-engine.js';
2: const KEY='dc500_current_v1';
5: export function archive(state,storage=localStorage){storage.setItem('dc500_archive_'+state.id,E.encode(state));}
6: export function archives(storage=localStorage){return Array.from({length:storage.length},(_,i)=>storage.key(i)).filter(k=>k.startsWith('dc500_archive_')).map(k=>E.decode(storage.getItem(k)));}
```

### web/engine.js ? 8 linhas candidatas

```text
1: export const initial=()=>({name:'Camp D. House',rounds:4,players:Array.from({length:12},(_,id)=>({id,name:'',deck:''})),teams:[],matches:[],started:false});
2: export const normalize=s=>s.trim().replace(/\s+/g,' ').toLowerCase();
5: if(players.length<4||players.length>24||players.length%4)throw Error('Use 4 a 24 participantes, em múltiplos de 4.');
6: if(players.some(p=>!p.name.trim()||!normalize(p.deck)))throw Error('Nome e deck são obrigatórios.');
9: if(Math.max(...Object.values(counts))>players.length/2)throw Error('Mais da metade usa o mesmo deck. Não há combinação válida.');
10: const teams=search([...players].sort(()=>Math.random()-.5));if(!teams)throw Error('Não há combinação válida.');return teams;
16: export function pairs(s){if(!s.started)throw Error('Confirme as duplas e inicie o campeonato.');if(s.matches.some(m=>m.events===null))throw Error('Confirme todos os resultados.');const round=Math.max(0,...s.matches.map(m=>m.round))+1;if(round>s.rounds)throw Error('Campeonato encerrado.');const scores=Object.fromEntries(ranking(s).map(x=>[x.team,x.points]));let best=Infinity,chosen=null;function search(rest,out,cost){if(cost>best)return;if(!rest.length){if(cost<best||Math.random()<.5){best=cost;chosen=out;}return;}const [a,...tail]=rest;for(const b of tail){if(s.matches.some(m=>m.a===a&&m.b===b||m.a===b&&m.b===a))continue;search(tail.filter(x=>x!==b),[...out,{a,b,round,events:null}],cost+Math.abs(scores[a]-scores[b]));}}search(s.teams.map((_,i)=>i),[],0);if(!chosen)throw Error('Não é possível gerar uma rodada sem repetir adversários.');return chosen;}
17: export function confirm(s,index,events){const m=s.matches[index];if(new Set(events.map(e=>e.victim)).size!==events.length)throw Error('Jogador eliminado mais de uma vez.');for(const e of events)if(!(s.teams[m.a].includes(e.killer)&&s.teams[m.b].includes(e.victim)||s.teams[m.b].includes(e.killer)&&s.teams[m.a].includes(e.victim)))throw Error('Eliminação inválida.');return {...s,matches:s.matches.map((m,i)=>i===index?{...m,events}:m)};}
```

### web/sw.js ? 4 linhas candidatas

```text
1: const CACHE='camp-d-house-v6';const ASSETS=['./','./index.html','./style.css','./app.js','./engine.js','./bootstrap.js','./cedh-engine.js','./cedh-app.js','./dc500-engine.js','./dc500-store.js','./dc500-app.js','./manifest.webmanifest','./icon.png'];
2: self.addEventListener('install',e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS))));
3: self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith('camp-d-house-')&&k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
4: self.addEventListener('fetch',e=>{if(e.request.method!=='GET'||new URL(e.request.url).origin!==self.location.origin)return;e.respondWith(caches.match(e.request).then(cached=>cached||fetch(e.request).catch(()=>e.request.mode==='navigate'?caches.match('./index.html'):Response.error())));});
```

### web/dc500.html ? 1 linhas candidatas

```text
1: <!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Duel Commander 500</title><link rel="stylesheet" href="./style.css"></head><body><header><h1>Duel Commander 500</h1><p id="status">Abrindo armazenamento?</p></header><nav aria-label="Navega??o"></nav><main id="app"></main><script type="module" src="./dc500-app.js"></script></body></html>
```

### web/index.html ? 1 linhas candidatas

```text
1: <!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover"><meta name="theme-color" content="#101827"><meta name="apple-mobile-web-app-capable" content="yes"><meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"><title>Championship Manager</title><link rel="icon" href="./icon.png"><link rel="manifest" href="./manifest.webmanifest"><link rel="apple-touch-icon" href="./icon.png"><link rel="stylesheet" href="./style.css"></head><body><header><small>CAMPEONATO • OFFLINE</small><img src="./icon.png" alt="Logo Championship Manager" width="88" height="88" style="border-radius:18px"><h1>Championship Manager</h1><p id="status">Abrindo armazenamento…</p></header><nav aria-label="Navegação"></nav><main id="app"></main><p class="hint">iPhone: Safari → Compartilhar → Adicionar à Tela de Início. Abra uma vez com internet antes de usar offline.</p><script type="module" src="./bootstrap.js"></script></body></html>
```

## Pend?ncias

- Todos os cat?logos pt-BR/en-US/es-ES e extra??o de textos.
- Seletor e bandeiras locais no Android; seletor na PWA.
- Detec??o, prefer?ncia persistida e atualiza??o reativa sem perda de edi??o.
- Formata??o, testes de locale, paridade de chaves e valida??o visual em telas pequenas.
- Builds e testes finais da implementa??o i18n.

Nenhuma regra de neg?cio foi alterada. Nenhum commit ou push.
