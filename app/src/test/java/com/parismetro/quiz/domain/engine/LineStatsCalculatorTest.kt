package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.StationProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LineStatsCalculatorTest {

    private fun line(id: String) = MetroLine(id, "Ligne $id", id, "#000000", "#FFFFFF", emptyList())
    private fun station(id: String, vararg lineIds: String) = Station(id, id, lineIds.toList(), 0f, 0f)

    private val lines = listOf(line("1"), line("2"), line("4"), line("14"))
    private val stations = listOf(
        station("chatelet", "1", "4", "14"),
        station("louvre", "1"),
        station("vavin", "4"),
        station("anvers", "2")
    )

    @Test
    fun `rate is first-try answers over questions seen, across the line's stations`() {
        val progress = listOf(
            StationProgress("chatelet", seen = 4, firstTry = 4),
            StationProgress("louvre", seen = 6, firstTry = 1)
        )
        val line1 = LineStatsCalculator.compute(lines, stations, progress).first { it.line.id == "1" }
        assertEquals(10, line1.seen)
        assertEquals(5, line1.firstTry)
        assertEquals(0.5f, line1.firstTryRate!!, 1e-6f)
    }

    @Test
    fun `an interchange counts for every line it serves`() {
        val progress = listOf(StationProgress("chatelet", seen = 2, firstTry = 1))
        val stats = LineStatsCalculator.compute(lines, stations, progress).associateBy { it.line.id }
        listOf("1", "4", "14").forEach { assertEquals(2, stats.getValue(it).seen) }
        assertEquals(0, stats.getValue("2").seen)
    }

    @Test
    fun `lines are sorted weakest first, never-played lines last in network order`() {
        val progress = listOf(
            StationProgress("louvre", seen = 4, firstTry = 3), // line 1: 75 %
            StationProgress("vavin", seen = 4, firstTry = 1) // line 4: 25 %
        )
        val order = LineStatsCalculator.compute(lines, stations, progress).map { it.line.id }
        assertEquals(listOf("4", "1", "2", "14"), order)
    }

    @Test
    fun `equal rates put the line with more answers first`() {
        val progress = listOf(
            StationProgress("louvre", seen = 2, firstTry = 1), // line 1: 50 % of 2
            StationProgress("vavin", seen = 10, firstTry = 5) // line 4: 50 % of 10
        )
        val order = LineStatsCalculator.compute(lines, stations, progress).map { it.line.id }
        assertEquals(listOf("4", "1"), order.take(2))
    }

    @Test
    fun `a never-played line has no rate`() {
        val stat = LineStatsCalculator.compute(lines, stations, emptyList()).first()
        assertNull(stat.firstTryRate)
        assertEquals("1", stat.line.id)
    }
}
