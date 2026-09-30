package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.Station
import kotlin.random.Random

/** Builds the multiple-choice options for a QCM question. */
object DistractorPicker {

    /**
     * Returns [optionCount] stations (or fewer if [pool] is too small), always including
     * [correctStation], in randomized order. Distractors sharing a line with the correct
     * station are preferred first since a name from a completely unrelated line is usually
     * too easy to rule out, which would defeat the point of the quiz.
     */
    fun pickOptions(
        correctStation: Station,
        pool: List<Station>,
        optionCount: Int = 4,
        random: Random = Random.Default
    ): List<Station> {
        val others = pool.filter { it.id != correctStation.id }
        val (sameLine, rest) = others.partition { candidate ->
            candidate.lineIds.any { it in correctStation.lineIds }
        }

        val distractors = (sameLine.shuffled(random) + rest.shuffled(random))
            .take((optionCount - 1).coerceAtLeast(0))

        return (distractors + correctStation).shuffled(random)
    }
}
