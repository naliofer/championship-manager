package com.campeonato.duplas
import com.campeonato.duplas.domain.CedhEngine as E
import com.campeonato.duplas.domain.CedhClock
import com.campeonato.duplas.data.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
class CedhEngineTest {
    private fun players(n:Int=8)=(1..n).map { E.Player("$it","Jogador $it","Comandante","Deck") }
    @Test fun fullTournamentAndPersistence() {
        for(n in listOf(8,12)) {
            var s=CedhState(config=E.Config(rounds=2),players=players(n))
            repeat(2) {
                val next=E.pair(s.players,s.tables,s.config,Random(it))
                assertEquals(n,next.flatMap { it.seats }.distinct().size)
                assertTrue(next.all { it.seats.size==4 })
                s=s.copy(tables=s.tables+next.map { t -> t.copy(result=E.Result(E.Outcome.WIN,t.seats[0])) })
                s=CedhCodec.decode(CedhCodec.encode(s))
            }
            assertEquals(n/4*10,E.standings(s.players,s.tables,s.config).sumOf { it.points })
            val corrected=s.tables.map { it.copy(result=E.Result(E.Outcome.TIME_DRAW,eliminated=mapOf(it.seats[0] to ""))) }
            assertTrue(E.standings(s.players,corrected,s.config).all { it.points==2 && it.draws==2 })
        }
    }
    @Test fun noDuplicateTablesAfterDraws() {
        val p=players(12);val c=E.Config()
        val first=E.pair(p,emptyList(),c,Random(1)).map { it.copy(result=E.Result(E.Outcome.TIME_DRAW)) }
        val next=E.pair(p,first,c,Random(2))
        assertTrue(next.none { t -> first.any { it.seats.toSet()==t.seats.toSet() } })
    }
    @Test(expected=IllegalArgumentException::class) fun rejectsDuplicatePlayer() {
        E.validate(players(4),listOf(E.Table(1,listOf("1","1","3","4"))),E.Config())
    }
    @Test fun topFourAndCorrection() {
        val p=players();val c=E.Config(rounds=1,top4=true)
        val tables=E.pair(p,emptyList(),c).map { it.copy(result=E.Result(E.Outcome.WIN,it.seats[0])) }
        val before=E.standings(p,tables,c);val final=E.finalTable(p,tables,c)
        assertEquals(before.take(4).map { it.id },final.seats)
        assertEquals(before,E.standings(p,tables+final.copy(result=E.Result(E.Outcome.WIN,final.seats[0])),c))
    }
    @Test fun timerSurvivesPauseAndReload() {
        var clock=CedhClock().start(1000)
        assertEquals(10000L,clock.elapsedAt(11000));clock=clock.pause(11000)
        assertEquals(10000L,clock.elapsedAt(90000));clock=clock.start(90000)
        val s=CedhState(clocks=mapOf(1 to clock))
        assertEquals(15000L,CedhCodec.decode(CedhCodec.encode(s)).clocks.getValue(1).elapsedAt(95000))
    }
    @Test fun omwCountsRepeatedOpponentOnlyOnce() {
        val p=players();val c=E.Config()
        val tables=listOf(
            E.Table(1,listOf("1","2","3","4"),E.Result(E.Outcome.WIN,"2")),
            E.Table(1,listOf("5","6","7","8"),E.Result(E.Outcome.WIN,"7")),
            E.Table(2,listOf("1","2","5","6"),E.Result(E.Outcome.WIN,"5")),
            E.Table(2,listOf("3","4","7","8"),E.Result(E.Outcome.WIN,"8")))
        E.validate(p,tables,c)
        assertEquals(0.2,E.standings(p,tables,c).first { it.id=="1" }.omw,0.000001)
    }
    @Test fun swissGroupsEqualPointsBeforeAvoidingOpponents() {
        val p=players(16);val c=E.Config()
        val first=E.pair(p,emptyList(),c,Random(1)).map { it.copy(result=E.Result(E.Outcome.WIN,it.seats[0])) }
        val winners=first.map { it.seats[0] }.toSet()
        val next=E.pair(p,first,c,Random(2))
        assertTrue(next.any { it.seats.toSet()==winners })
    }
    @Test fun eightDrawnPlayersReachMinimumRepeatPairs() {
        val p=players();val c=E.Config()
        val first=E.pair(p,emptyList(),c).map { it.copy(result=E.Result(E.Outcome.TIME_DRAW)) }
        val next=E.pair(p,first,c)
        val repeats=next.sumOf { t -> first.sumOf { old -> val n=t.seats.count { it in old.seats };n*(n-1)/2 } }
        assertEquals(4,repeats)
    }
    @Test fun administrativeAdjustmentAndCustomPoints() {
        val p=players(4);val c=E.Config(win=7,draw=2,loss=1)
        val t=E.pair(p,emptyList(),c).first()
        val id=t.seats[0]
        val draw=t.copy(result=E.Result(E.Outcome.TIME_DRAW,penalties=mapOf(id to -2)))
        assertEquals(0,E.standings(p,listOf(draw),c).first { it.id==id }.points)
        val win=t.copy(result=E.Result(E.Outcome.WIN,id))
        val rows=E.standings(p,listOf(win),c)
        assertEquals(7,rows.first().points);assertTrue(rows.drop(1).all { it.points==1 && it.losses==1 })
    }
}
