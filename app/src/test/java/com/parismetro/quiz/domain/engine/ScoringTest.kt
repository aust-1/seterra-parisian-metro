package com.parismetro.quiz.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    @Test
    fun `awards fewer points after each miss`() {
        assertEquals(3, scoreForCorrectGuess(0))
        assertEquals(2, scoreForCorrectGuess(1))
        assertEquals(1, scoreForCorrectGuess(2))
    }

    @Test
    fun `never awards points at or past the miss limit`() {
        assertEquals(0, scoreForCorrectGuess(3))
        assertEquals(0, scoreForCorrectGuess(4))
        assertEquals(0, scoreForReveal())
    }

    @Test
    fun `instant answer is all-or-nothing`() {
        assertEquals(3, scoreForInstantAnswer(true))
        assertEquals(0, scoreForInstantAnswer(false))
    }
}
