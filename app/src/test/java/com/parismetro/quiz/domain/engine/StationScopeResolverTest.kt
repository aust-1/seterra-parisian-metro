package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.StationScope
import org.junit.Assert.assertEquals
import org.junit.Test

class StationScopeResolverTest {

    private fun station(id: String, vararg lines: String) =
        Station(id = id, name = id, lineIds = lines.toList(), x = 0f, y = 0f)

    private val stations = listOf(
        station("a", "1"),
        station("b", "1", "4"),
        station("c", "9"),
        station("d", "1", "9", "14")
    )

    @Test
    fun `all stations returns everything`() {
        assertEquals(stations, StationScopeResolver.resolve(StationScope.AllStations, stations))
    }

    @Test
    fun `essentials keeps only interchange stations`() {
        val result = StationScopeResolver.resolve(StationScope.Essentials, stations)
        assertEquals(setOf("b", "d"), result.map { it.id }.toSet())
    }

    @Test
    fun `by lines keeps stations served by at least one selected line`() {
        val result = StationScopeResolver.resolve(StationScope.ByLines(setOf("9")), stations)
        assertEquals(setOf("c", "d"), result.map { it.id }.toSet())
    }

    @Test
    fun `by lines with no matching line returns empty`() {
        val result = StationScopeResolver.resolve(StationScope.ByLines(setOf("99")), stations)
        assertEquals(emptyList<Station>(), result)
    }
}
