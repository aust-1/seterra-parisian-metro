package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DistractorPickerTest {

    private fun station(id: String, vararg lines: String) =
        Station(id = id, name = id, lineIds = lines.toList(), x = 0f, y = 0f)

    @Test
    fun `always includes the correct station`() {
        val correct = station("a", "1")
        val pool = listOf(correct, station("b", "1"), station("c", "2"), station("d", "3"))

        val options = DistractorPicker.pickOptions(correct, pool, optionCount = 3, random = Random(1))

        assertEquals(3, options.size)
        assertTrue(options.contains(correct))
    }

    @Test
    fun `never duplicates a station`() {
        val correct = station("a", "1")
        val pool = listOf(correct, station("b", "1"), station("c", "2"))

        val options = DistractorPicker.pickOptions(correct, pool, optionCount = 4, random = Random(1))

        assertEquals(options.size, options.map { it.id }.distinct().size)
    }

    @Test
    fun `shrinks gracefully when the pool is smaller than the requested option count`() {
        val correct = station("a", "1")
        val pool = listOf(correct, station("b", "1"))

        val options = DistractorPicker.pickOptions(correct, pool, optionCount = 4, random = Random(1))

        assertEquals(2, options.size)
    }

    @Test
    fun `prefers stations sharing a line with the correct one`() {
        val correct = station("a", "1")
        val sameLine = station("b", "1")
        val otherLine = station("c", "9")
        val pool = listOf(correct, sameLine, otherLine)

        val options = DistractorPicker.pickOptions(correct, pool, optionCount = 2, random = Random(1))

        assertTrue(options.contains(sameLine))
        assertFalse(options.contains(otherLine))
    }
}
