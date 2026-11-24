package com.parismetro.quiz.domain.engine

import java.text.Normalizer
import java.util.Locale

/**
 * Normalizes free-typed station names so "Alma-Marceau", "alma marceau" and "alma  marceau"
 * (and accented input like "Chateau" for "Château") all compare equal.
 *
 * On top of that, [matches] accepts the names people actually type:
 * - the usual abbreviations, "St"/"Ste"/"Pte" for "Saint"/"Sainte"/"Porte" ("St Lazare",
 *   "Pte de Clichy");
 * - the name without its precision in parentheses ("Pont Marie" for "Pont Marie (Cité des
 *   Arts)", "Saint-Paul" for "Saint-Paul (Le Marais)") - typing the full name still works.
 */
object AnswerNormalizer {

    /** Whole-word abbreviations, expanded after normalization (so "St." and "st" both match). */
    private val ABBREVIATIONS = mapOf(
        "st" to "saint",
        "ste" to "sainte",
        "pte" to "porte"
    )

    private val PARENTHESIZED = Regex("\\([^)]*\\)")

    fun normalize(input: String): String {
        val lower = input.trim().lowercase(Locale.FRANCE)
        val withoutDiacritics = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutDiacritics
            .replace(Regex("[-'’`_.,()]+"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word -> ABBREVIATIONS[word] ?: word }
    }

    /** Every normalized spelling accepted for [expected]: in full, and without "(…)". */
    fun acceptedForms(expected: String): Set<String> = setOf(
        normalize(expected),
        normalize(expected.replace(PARENTHESIZED, " "))
    ).filterTo(mutableSetOf()) { it.isNotEmpty() }

    fun matches(input: String, expected: String): Boolean =
        input.isNotBlank() && normalize(input) in acceptedForms(expected)
}
