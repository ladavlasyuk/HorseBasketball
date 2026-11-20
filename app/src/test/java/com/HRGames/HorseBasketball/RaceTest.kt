package com.HRGames.HorseBasketball

import org.junit.Assert.*
import org.junit.Test
import java.io.*

class RaceTest {
    private fun race(hits: (Int) -> Boolean): Race {
        val race = Race()
        repeat(5000) {
            if (race.inRange && hits(race.attempts)) race.shoot()
            race.step()
            if (race.ended) return race
        }
        error("Race did not end")
    }

    @Test fun allBasketsWinAndRespectSpeedLimit() {
        val r = race { true }
        assertTrue(r.finished)
        assertEquals(1, r.place)
        assertEquals(11, r.baskets)
        assertEquals(18.0, r.peak, 0.0)
    }

    @Test fun missingEveryHoopTimesOut() {
        val r = race { false }
        assertFalse(r.finished)
        assertEquals(60.0, r.elapsed, 1e-7)
        assertEquals(5.0, r.speed, 0.0)
        assertEquals(0, r.baskets)
    }

    @Test fun mixedRunCanFinishBehindRival() {
        val r = race { it % 2 == 1 }
        assertTrue(r.finished)
        assertTrue(r.place > 1)
    }

    @Test fun earlyAndLateShotsOnlyPenalizeOnce() {
        for (distance in listOf(2.0, 33.0)) {
            val r = Race().apply { countdown = 0.0; this.distance = distance }
            repeat(20) { r.shoot() }
            repeat(70) { r.step() }
            assertEquals(1, r.attempts)
            assertEquals(Call.Miss, r.call)
            assertEquals(7.0, r.speed, 0.0)
        }
    }

    @Test fun boostSlowsOnlyTheClockForThreeSeconds() {
        val r = Race().apply { countdown = 0.0; distance = nextHoop - reach }
        r.shoot()
        repeat(49) { r.step() }
        assertEquals(1, r.baskets)
        val before = r.clock
        val position = r.distance
        repeat(60) { r.step() }
        assertEquals(.5, before - r.clock, 1e-7)
        assertEquals(11.5, r.distance - position, 1e-7)
        repeat(120) { r.step() }
        assertEquals(0.0, r.boost, 1e-7)
    }

    @Test fun finishAndTimeoutUseSubframeOrder() {
        for (offset in listOf(-.001, 0.0, .001)) {
            val r = Race().apply { countdown = 0.0; nextHoop = 640.0; distance = TRACK - speed * .010; clock = .010 + offset }
            r.step()
            assertTrue(r.ended)
            assertEquals(offset >= 0, r.finished)
        }
    }

    @Test fun serializationPreservesAnAirborneBall() {
        val r = Race().apply { countdown = 0.0; distance = nextHoop - reach; shoot() }
        repeat(15) { r.step() }
        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(r) }
        val restored = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() as Race }
        repeat(150) { r.step(); restored.step() }
        assertEquals(r.baskets, restored.baskets)
        assertEquals(r.clock, restored.clock, 0.0)
        assertEquals(r.distance, restored.distance, 0.0)
    }
}
