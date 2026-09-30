package com.parismetro.quiz.domain.engine

import kotlin.random.Random

data class AnswerQuestion(
    val stationId: String,
    val status: QuestionStatus = QuestionStatus.GUESSING,
    val pointsAwarded: Int = 0
)

data class AnswerQuizState(
    val stationQueue: List<String>,
    val currentIndex: Int = 0,
    val score: Int = 0,
    val status: GameStatus,
    val currentQuestion: AnswerQuestion?,
    val completedStationIds: List<String> = emptyList()
)

data class AnswerResult(
    val state: AnswerQuizState,
    val outcome: GuessOutcome,
    val pointsAwarded: Int
)

/**
 * Shared engine for the QCM and type-the-name modes: unlike [MapClickQuizEngine], an answer
 * locks in the moment it's submitted (real-Seterra quiz behaviour) - there is no retry loop,
 * just correct/incorrect and move on.
 */
object AnswerQuizEngine {

    fun createQuiz(stationIds: List<String>, random: Random = Random.Default): AnswerQuizState {
        val queue = stationIds.shuffled(random)
        return AnswerQuizState(
            stationQueue = queue,
            currentIndex = 0,
            score = 0,
            status = if (queue.isNotEmpty()) GameStatus.PLAYING else GameStatus.COMPLETED,
            currentQuestion = queue.firstOrNull()?.let { AnswerQuestion(stationId = it) },
            completedStationIds = emptyList()
        )
    }

    /** Submits the one and only answer for the current question. */
    fun submitAnswer(state: AnswerQuizState, isCorrect: Boolean): AnswerResult {
        val question = state.currentQuestion
        if (question == null || state.status == GameStatus.COMPLETED || question.status != QuestionStatus.GUESSING) {
            return AnswerResult(state, GuessOutcome.IGNORED, 0)
        }

        val pointsAwarded = scoreForInstantAnswer(isCorrect)
        val nextQuestion = question.copy(
            status = if (isCorrect) QuestionStatus.CORRECT else QuestionStatus.REVEALED,
            pointsAwarded = pointsAwarded
        )
        val nextState = state.copy(score = state.score + pointsAwarded, currentQuestion = nextQuestion)
        val outcome = if (isCorrect) GuessOutcome.CORRECT else GuessOutcome.INCORRECT
        return AnswerResult(nextState, outcome, pointsAwarded)
    }

    fun goToNextQuestion(state: AnswerQuizState): AnswerQuizState {
        val question = state.currentQuestion
        if (question == null || question.status == QuestionStatus.GUESSING) {
            return state
        }

        val completedStationIds = state.completedStationIds + question.stationId
        val nextIndex = state.currentIndex + 1
        val nextStationId = state.stationQueue.getOrNull(nextIndex)
            ?: return state.copy(
                currentIndex = nextIndex,
                status = GameStatus.COMPLETED,
                currentQuestion = null,
                completedStationIds = completedStationIds
            )

        return state.copy(
            currentIndex = nextIndex,
            currentQuestion = AnswerQuestion(stationId = nextStationId),
            completedStationIds = completedStationIds
        )
    }

    fun restartQuiz(state: AnswerQuizState, random: Random = Random.Default): AnswerQuizState =
        createQuiz(state.stationQueue, random)
}
