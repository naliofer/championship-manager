package com.campeonato.duplas.domain

import kotlin.random.Random

/** Independent multiplayer rules: seats are ordered, results are replaced rather than accumulated. */
object CedhEngine {
    data class Player(val id: String, val name: String, val commanders: String = "", val deck: String = "", val moxfield: String = "")
    data class Config(val rounds: Int = 4, val minutes: Int = 75, val extraMinutes: Int = 10,
        val win: Int = 5, val draw: Int = 1, val loss: Int = 0, val top4: Boolean = false, val agreement: Boolean = false,
        val finalMinutes: Int = 75, val finalExtraMinutes: Int = 10)
    enum class Outcome { WIN, TIME_DRAW, AGREEMENT_DRAW, ADMINISTRATIVE }
    data class Result(val outcome: Outcome, val winner: String? = null, val eliminated: Map<String, String> = emptyMap(),
        val durationSeconds: Int = 0, val notes: String = "", val penalties: Map<String, Int> = emptyMap())
    data class Table(val round: Int, val seats: List<String>, val result: Result? = null, val final: Boolean = false)
    data class Standing(val id: String, val points: Int, val wins: Int, val draws: Int, val losses: Int, val games: Int, val omw: Double) {
        val winRate: Double get() = if (games == 0) 0.0 else wins.toDouble() / games
    }
    fun validate(players: List<Player>, tables: List<Table>, config: Config) {
        require(players.size >= 4 && players.size % 4 == 0)
        val ids = players.map { it.id }.toSet()
        require(ids.size == players.size && players.all { it.id.isNotBlank() && it.name.isNotBlank() })
        require(config.rounds > 0 && config.minutes > 0 && config.extraMinutes >= 0)
        require(config.finalMinutes > 0 && config.finalExtraMinutes >= 0)
        require(config.win >= 0 && config.draw >= 0 && config.loss >= 0)
        val rounds = tables.map { it.round }.distinct().sorted()
        require(rounds == (1..(rounds.lastOrNull() ?: 0)).toList())
        tables.groupBy { it.round }.forEach { (_, games) ->
            val seats = games.flatMap { it.seats }
            require(games.all { it.final == games.first().final })
            require(games.all { if(it.final) config.top4 && it.round == config.rounds + 1 else it.round in 1..config.rounds })
            require(games.all { t -> tables.filter { it.round < t.round }.all { it.result != null } })
            require(seats.distinct().size == seats.size && seats.all { it in ids })
            require(if (games.all { it.final }) games.size == 1 else seats.toSet() == ids)
            games.forEach { t ->
                require(t.seats.size == 4)
                t.result?.let { r ->
                    require((r.outcome == Outcome.WIN) == (r.winner != null))
                    require(r.winner == null || r.winner in t.seats)
                    require(!t.final || r.winner != null)
                    require(r.outcome != Outcome.AGREEMENT_DRAW || config.agreement)
                    require(r.eliminated.keys.all { it in t.seats && it != r.winner })
                    require(r.penalties.keys.all { it in t.seats } && r.durationSeconds >= 0)
                }
            }
        }
    }
    fun standings(players: List<Player>, tables: List<Table>, config: Config): List<Standing> {
        val played = tables.filter { !it.final && it.result != null }
        val base = players.associate { p ->
            val games = played.filter { p.id in it.seats }
            val wins = games.count { it.result!!.winner == p.id }
            val draws = games.count { it.result!!.outcome in listOf(Outcome.TIME_DRAW, Outcome.AGREEMENT_DRAW) }
            val losses = games.size - wins - draws
            p.id to Standing(p.id, wins * config.win + draws * config.draw + losses * config.loss + games.sumOf { it.result!!.penalties[p.id] ?: 0 }, wins, draws, losses, games.size, 0.0)
        }
        return base.values.map { s ->
            val opponents = played.filter { s.id in it.seats }.flatMap { it.seats }.toSet() - s.id
            s.copy(omw = if (opponents.isEmpty()) 0.0 else opponents.map { base.getValue(it).winRate }.average())
        }.sortedWith(compareByDescending<Standing> { it.points }.thenByDescending { it.wins }.thenByDescending { it.omw }.thenBy { it.id })
    }
    /** Lexicographic local optimization of score spread, repeated opponents and seat imbalance. */
    fun pair(players: List<Player>, history: List<Table>, config: Config, random: Random = Random.Default): List<Table> {
        validate(players, history, config)
        require(history.all { it.result != null } && history.none { it.final })
        val round = (history.maxOfOrNull { it.round } ?: 0) + 1
        require(round <= config.rounds)
        val points = standings(players, history, config).associate { it.id to it.points }
        fun cost(order: List<String>): List<Long> {
            var spread = 0L; var repeats = 0L; var seats = 0L
            order.chunked(4).forEach { group ->
                spread += (group.maxOf { points.getValue(it) } - group.minOf { points.getValue(it) }).toLong()
                history.forEach { old ->
                    val common = group.count { it in old.seats }
                    repeats += common * (common - 1) / 2 + if (common == 4) 100 else 0
                }
                group.forEachIndexed { index, id -> seats += history.count { it.seats[index] == id }.toLong() }
            }
            return listOf(spread, repeats, seats)
        }
        fun better(a: List<Long>, b: List<Long>): Boolean { for (i in a.indices) if (a[i] != b[i]) return a[i] < b[i]; return false }
        var best = players.map { it.id }.shuffled(random)
        if (history.isNotEmpty()) {
            best = best.sortedByDescending { points.getValue(it) }
            var score = cost(best)
            repeat(20) {
                var candidate = if (it == 0) best else best.shuffled(random).sortedByDescending { points.getValue(it) }
                var value = cost(candidate)
                var improved = true
                while (improved) {
                    improved = false
                    for (a in candidate.indices) for (b in a + 1 until candidate.size) {
                        val swap = candidate.toMutableList().apply { this[a] = candidate[b]; this[b] = candidate[a] }
                        val next = cost(swap)
                        if (better(next, value)) { candidate = swap; value = next; improved = true }
                    }
                }
                if (better(value, score)) { best = candidate; score = value }
            }
        }
        return best.chunked(4).map { Table(round, it) }
    }
    fun finalTable(players: List<Player>, history: List<Table>, config: Config): Table {
        validate(players, history, config)
        require(config.top4 && history.none { it.final } && history.maxOfOrNull { it.round } == config.rounds && history.all { it.result != null })
        return Table(config.rounds + 1, standings(players, history, config).take(4).map { it.id }, final = true)
    }
}
