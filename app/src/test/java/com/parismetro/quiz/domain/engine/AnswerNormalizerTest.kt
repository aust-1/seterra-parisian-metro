package com.parismetro.quiz.domain.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerNormalizerTest {

    @Test
    fun `matches regardless of case`() {
        assertTrue(AnswerNormalizer.matches("chatelet", "Chatelet"))
    }

    @Test
    fun `matches regardless of accents`() {
        assertTrue(AnswerNormalizer.matches("Chatelet", "Châtelet"))
        assertTrue(AnswerNormalizer.matches("republique", "République"))
    }

    @Test
    fun `treats hyphens, apostrophes and extra spaces like single spaces`() {
        assertTrue(AnswerNormalizer.matches("alma marceau", "Alma - Marceau"))
        assertTrue(AnswerNormalizer.matches("Reaumur Sebastopol", "Réaumur - Sébastopol"))
        assertTrue(AnswerNormalizer.matches("chateau d eau", "Château d'Eau"))
    }

    @Test
    fun `rejects a genuinely wrong answer`() {
        assertFalse(AnswerNormalizer.matches("Bastille", "Nation"))
    }

    @Test
    fun `rejects blank input`() {
        assertFalse(AnswerNormalizer.matches("   ", "Nation"))
    }
}
