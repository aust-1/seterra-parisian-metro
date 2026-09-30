package com.parismetro.quiz.domain.engine

import java.text.Normalizer
import java.util.Locale

/**
 * Normalizes free-typed station names so "Alma-Marceau", "alma marceau" and "alma  marceau"
 * (and accented input like "Chateau" for "Château") all compare equal.
 */
object AnswerNormalizer {

    fun normalize(input: String): String {
        val lower = input.trim().lowercase(Locale.FRANCE)
        val withoutDiacritics = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutDiacritics
            .replace(Regex("[-'’`_]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun matches(input: String, expected: String): Boolean =
        input.isNotBlank() && normalize(input) == normalize(expected)
}
