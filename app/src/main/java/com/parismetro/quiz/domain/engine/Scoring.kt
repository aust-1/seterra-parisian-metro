package com.parismetro.quiz.domain.engine

/** Maximum wrong taps allowed in map-click mode before the answer is auto-revealed. */
const val MAX_MISSES = 3

/**
 * Points for a correct map-click guess: full marks on the first try, one less per miss,
 * down to zero once [missCount] reaches [MAX_MISSES] (at which point the question would
 * already have been auto-revealed).
 */
fun scoreForCorrectGuess(missCount: Int): Int = (MAX_MISSES - missCount).coerceAtLeast(0)

/** A revealed (given up on) map-click question always scores zero. */
fun scoreForReveal(): Int = 0

/** QCM / type-answer modes are single-shot: full marks or nothing, no partial credit. */
fun scoreForInstantAnswer(correct: Boolean): Int = if (correct) MAX_MISSES else 0
