package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapExplorerTest {

    private fun line(id: String, label: String = id) = MetroLine(id, "Ligne $label", label, "#000000", "#FFFFFF", emptyList())
    private fun station(id: String, vararg lineIds: String) = Station(id, id, lineIds.toList(), 0f, 0f)

    private val lines = listOf(line("1"), line("3b", "3bis"), line("4"), line("7"))
    private val stations = listOf(
        station("chatelet", "1", "4", "7"),
        station("gambetta", "3b"),
        station("vavin", "4")
    )

    @Test
    fun `no line selected shows the whole network`() {
        assertEquals(stations, MapExplorer.visibleStations(stations, emptySet()))
        assertEquals(lines, MapExplorer.visibleLines(lines, emptySet()))
    }

    @Test
    fun `selected lines filter stations and lines like the game setup`() {
        assertEquals(listOf("chatelet", "vavin"), MapExplorer.visibleStations(stations, setOf("4")).map { it.id })
        assertEquals(listOf("4"), MapExplorer.visibleLines(lines, setOf("4")).map { it.id })
    }

    @Test
    fun `lines of a station come in network order`() {
        val reordered = station("x", "7", "1")
        assertEquals(listOf("1", "7"), MapExplorer.linesOf(reordered, lines).map { it.id })
    }

    @Test
    fun `transfer summary lists the lines of an interchange`() {
        assertEquals(
            "Correspondance entre les lignes 1, 4 et 7",
            MapExplorer.transferSummary(stations[0], lines)
        )
        assertNull(MapExplorer.transferSummary(stations[1], lines))
    }
}
