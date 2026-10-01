package com.parismetro.quiz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GameConfigTest {

    @Test
    fun `storage key is stable regardless of line selection order`() {
        val a = GameConfig(GameMode.MAP_CLICK, StationScope.ByLines(setOf("9", "1", "4")), showLines = true)
        val b = GameConfig(GameMode.MAP_CLICK, StationScope.ByLines(setOf("4", "9", "1")), showLines = true)

        assertEquals(a.storageKey(), b.storageKey())
    }

    @Test
    fun `storage key distinguishes mode, scope, line visibility and found-stations display`() {
        val base = GameConfig(GameMode.MULTIPLE_CHOICE, StationScope.AllStations, showLines = true)

        assertNotEquals(base.storageKey(), base.copy(mode = GameMode.TYPE_ANSWER).storageKey())
        assertNotEquals(base.storageKey(), base.copy(scope = StationScope.Essentials).storageKey())
        assertNotEquals(base.storageKey(), base.copy(showLines = false).storageKey())
        assertNotEquals(
            base.storageKey(),
            base.copy(foundStationsDisplay = FoundStationsDisplay.COLORED_LOCKED).storageKey()
        )
    }
}
