package com.campeonato.duplas.data

import android.content.SharedPreferences
import com.campeonato.duplas.domain.CedhEngine as E
import com.campeonato.duplas.domain.CedhClock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CedhState(val id: String = UUID.randomUUID().toString(), val name: String = "cEDH Multiplayer",
    val config: E.Config = E.Config(), val players: List<E.Player> = emptyList(), val tables: List<E.Table> = emptyList(),
    val clocks: Map<Int, CedhClock> = emptyMap())
object CedhCodec {
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.strings() = keys().asSequence().associateWith { getString(it) }
    fun encode(s: CedhState): String = JSONObject().apply {
        put("version", 1); put("id", s.id); put("name", s.name)
        put("config", JSONObject().apply { val c = s.config
            put("rounds", c.rounds); put("minutes", c.minutes); put("extra", c.extraMinutes)
            put("win", c.win); put("draw", c.draw); put("loss", c.loss); put("top4", c.top4); put("agreement", c.agreement)
            put("finalMinutes",c.finalMinutes); put("finalExtra",c.finalExtraMinutes)
        })
        put("players", JSONArray().apply { s.players.forEach { p -> put(JSONObject().apply {
            put("id",p.id); put("name",p.name); put("commanders",p.commanders); put("deck",p.deck); put("moxfield",p.moxfield)
        }) } })
        put("tables", JSONArray().apply { s.tables.forEach { t -> put(JSONObject().apply {
            put("round",t.round); put("seats",JSONArray(t.seats)); put("final",t.final)
            t.result?.let { r -> put("result",JSONObject().apply {
                put("outcome",r.outcome.name); put("winner",r.winner); put("eliminated",JSONObject(r.eliminated))
                put("duration",r.durationSeconds); put("notes",r.notes); put("penalties",JSONObject(r.penalties))
            }) }
        }) } })
        put("clocks",JSONObject().apply { s.clocks.forEach { (round,c) -> put(round.toString(),JSONObject().put("elapsed",c.elapsed).put("since",c.since)) } })
    }.toString()
    fun decode(raw: String): CedhState {
        val j=JSONObject(raw); require(j.getInt("version")==1)
        val c=j.getJSONObject("config")
        val config=E.Config(c.getInt("rounds"),c.getInt("minutes"),c.getInt("extra"),c.getInt("win"),c.getInt("draw"),c.getInt("loss"),c.getBoolean("top4"),c.getBoolean("agreement"),c.optInt("finalMinutes",75),c.optInt("finalExtra",10))
        val players=j.getJSONArray("players").objects().map { E.Player(it.getString("id"),it.getString("name"),it.getString("commanders"),it.getString("deck"),it.getString("moxfield")) }
        val tables=j.getJSONArray("tables").objects().map { t ->
            val seats=t.getJSONArray("seats"); val r=t.optJSONObject("result")
            E.Table(t.getInt("round"),(0 until seats.length()).map { seats.getString(it) },r?.let {
                E.Result(E.Outcome.valueOf(it.getString("outcome")),if(it.has("winner"))it.getString("winner") else null,
                    it.getJSONObject("eliminated").strings(),it.getInt("duration"),it.getString("notes"),
                    it.getJSONObject("penalties").let { p -> p.keys().asSequence().associateWith { k -> p.getInt(k) } })
            },t.getBoolean("final"))
        }
        val clocks=j.getJSONObject("clocks").let { cs -> cs.keys().asSequence().associate { key ->
            val v=cs.getJSONObject(key); key.toInt() to CedhClock(v.getLong("elapsed"),if(v.has("since"))v.getLong("since") else null)
        } }
        return CedhState(j.getString("id"),j.getString("name"),config,players,tables,clocks).also(::validate)
    }
    fun validate(s: CedhState) {
        require(s.name.isNotBlank()); require(s.config.rounds>0 && s.config.minutes>0 && s.config.extraMinutes>=0)
        require(s.config.finalMinutes>0 && s.config.finalExtraMinutes>=0)
        require(s.config.win>=0 && s.config.draw>=0 && s.config.loss>=0)
        require(s.players.map { it.id }.distinct().size==s.players.size)
        require(s.id.isNotBlank() && s.players.all { it.id.isNotBlank() })
        require(s.clocks.all { (r,c) -> r>0 && c.elapsed>=0 && (c.since==null || c.since>=0) })
        if(s.tables.isNotEmpty()) E.validate(s.players,s.tables,s.config)
    }
}
class CedhRepository(private val prefs: SharedPreferences) {
    fun archives(): List<CedhState> = prefs.all.keys.filter { it.startsWith("cedh_archive_") }.mapNotNull { prefs.getString(it,null)?.let(CedhCodec::decode) }
    fun load(): CedhState = prefs.getString("cedh_current",null)?.let(CedhCodec::decode) ?: CedhState()
    fun save(s: CedhState) { CedhCodec.validate(s); check(prefs.edit().putString("cedh_current",CedhCodec.encode(s)).commit()) { "Falha ao salvar cEDH" } }
    fun archive(s: CedhState) { check(prefs.edit().putString("cedh_archive_${s.id}",CedhCodec.encode(s)).commit()) }
}
