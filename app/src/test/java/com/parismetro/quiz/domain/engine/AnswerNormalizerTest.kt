package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.Station
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
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
    fun `the precision in parentheses is optional`() {
        assertTrue(AnswerNormalizer.matches("Pont Marie", "Pont Marie (Cité des Arts)"))
        assertTrue(AnswerNormalizer.matches("Saint-Paul", "Saint-Paul (Le Marais)"))
        assertTrue(AnswerNormalizer.matches("la defense", "La Défense (Grande Arche)"))
        assertTrue(AnswerNormalizer.matches("Chevilly Larue", "Chevilly-Larue (Marché International)"))
        assertTrue(AnswerNormalizer.matches("Thiais Orly", "Thiais - Orly (Pont de Rungis)"))
    }

    @Test
    fun `the full name with its precision is still accepted`() {
        assertTrue(AnswerNormalizer.matches("Pont Marie (Cité des Arts)", "Pont Marie (Cité des Arts)"))
        assertTrue(AnswerNormalizer.matches("saint paul le marais", "Saint-Paul (Le Marais)"))
    }

    @Test
    fun `the precision alone is not the station name`() {
        assertFalse(AnswerNormalizer.matches("Cité des Arts", "Pont Marie (Cité des Arts)"))
        assertFalse(AnswerNormalizer.matches("Pont de Rungis", "Thiais - Orly (Pont de Rungis)"))
    }

    @Test
    fun `expands the usual abbreviations St, Ste and Pte`() {
        assertTrue(AnswerNormalizer.matches("St Lazare", "Saint-Lazare"))
        assertTrue(AnswerNormalizer.matches("St-Lazare", "Saint-Lazare"))
        assertTrue(AnswerNormalizer.matches("st. paul", "Saint-Paul (Le Marais)"))
        assertTrue(AnswerNormalizer.matches("Pte de Clichy", "Porte de Clichy"))
        assertTrue(AnswerNormalizer.matches("Ste Anne", "Sainte-Anne"))
    }

    @Test
    fun `abbreviations only expand as whole words`() {
        assertEquals("bastille", AnswerNormalizer.normalize("Bastille"))
        assertEquals("saint lazare", AnswerNormalizer.normalize("St. Lazare"))
    }

    @Test
    fun `no two bundled stations share an accepted spelling`() {
        val file = listOf("src/main/assets/metro-data/stations.json", "app/src/main/assets/metro-data/stations.json")
            .map(::File)
            .first { it.exists() }
        val names = Json { ignoreUnknownKeys = true }
            .decodeFromString<List<Station>>(file.readText())
            .map { it.name }
        val owners = names.flatMap { name -> AnswerNormalizer.acceptedForms(name).map { it to name } }
            .groupBy({ it.first }, { it.second })
        val ambiguous = owners.filterValues { it.size > 1 }
        assertTrue("spellings accepted for several stations: $ambiguous", ambiguous.isEmpty())
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
