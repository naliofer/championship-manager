package com.campeonato.duplas
import android.content.SharedPreferences
import com.campeonato.duplas.data.*
import com.campeonato.duplas.domain.CedhEngine as E
import com.campeonato.duplas.domain.CedhClock
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Test

class CedhPersistenceTest {
    private class Memory {
        val values=mutableMapOf<String,Any?>()
        var works=true
        val prefs=Proxy.newProxyInstance(SharedPreferences::class.java.classLoader,arrayOf(SharedPreferences::class.java)) { _,method,args ->
            when(method.name) {
                "getString" -> values[args!![0]] ?: args!![1]
                "getAll" -> values.toMap()
                "edit" -> {
                    val pending=mutableMapOf<String,Any?>()
                    Proxy.newProxyInstance(SharedPreferences.Editor::class.java.classLoader,arrayOf(SharedPreferences.Editor::class.java)) { proxy,m,a ->
                        when(m.name) { "putString" -> {pending[a!![0] as String]=a[1];proxy}; "commit" -> {if(works)values.putAll(pending);works};else -> error(m.name) }
                    }
                }
                else -> error(method.name)
            }
        } as SharedPreferences
    }
    @Test fun repositoryFlowReloadCorrectionArchiveAndModalityIsolation() {
        val memory=Memory()
        val legacy=ChampionshipRepository(memory.prefs).load()
        ChampionshipRepository(memory.prefs).save(legacy)
        val original=memory.values["championship_v2"]
        var s=CedhState(players=(1..8).map { E.Player("$it","Jogador $it","Mesmo comandante","Deck") },config=E.Config(rounds=1,top4=true))
        val repo=CedhRepository(memory.prefs)
        repo.save(s);assertEquals(s,CedhRepository(memory.prefs).load())
        s=s.copy(tables=E.pair(s.players,s.tables,s.config).map { it.copy(result=E.Result(E.Outcome.WIN,it.seats[0])) },clocks=mapOf(1 to CedhClock().start(1000)))
        repo.save(s);s=CedhRepository(memory.prefs).load()
        val corrected=s.tables.map { it.copy(result=E.Result(E.Outcome.TIME_DRAW)) }
        s=s.copy(tables=corrected);repo.save(s);repo.save(s)
        assertTrue(E.standings(s.players,CedhRepository(memory.prefs).load().tables,s.config).all { it.points==1 })
        val final=E.finalTable(s.players,s.tables,s.config)
        s=s.copy(tables=s.tables+final.copy(result=E.Result(E.Outcome.WIN,final.seats[0])))
        repo.save(s);repo.archive(s);repo.save(CedhState())
        assertEquals(s,repo.archives().single());repo.save(repo.archives().single())
        assertEquals(s,CedhRepository(memory.prefs).load())
        assertEquals(5000L,s.clocks.getValue(1).elapsedAt(6000))
        assertEquals(original,memory.values["championship_v2"])
        assertEquals(legacy,ChampionshipRepository(memory.prefs).load())
    }
    @Test fun writeFailureAndCorruptionPreserveSavedData() {
        val memory=Memory();val repo=CedhRepository(memory.prefs);val s=CedhState();repo.save(s)
        memory.works=false
        try{repo.save(s.copy(name="Outro"));fail("Falha não propagada")}catch(_:IllegalStateException){}
        assertEquals(s,repo.load())
        memory.values["cedh_current"]="invalid"
        try{repo.load();fail("Corrupção aceita")}catch(_:Exception){}
        assertEquals("invalid",memory.values["cedh_current"])
    }
}
