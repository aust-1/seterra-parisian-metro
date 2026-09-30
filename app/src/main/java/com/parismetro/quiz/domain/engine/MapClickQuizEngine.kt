package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.MissMarker
import kotlin.random.Random

enum class QuestionStatus { GUESSING, CORRECT, REVEALED }
enum class GameStatus { PLAYING, COMPLETED }
enum class GuessOutcome { CORRECT, INCORRECT, REVEALED, IGNORED }

data class MapClickQuestion(
    val stationId: String,
    val status: QuestionStatus = QuestionStatus.GUESSING,
    val missCount: Int = 0,
    val missMarkers: List<MissMarker> = emptyList(),
    val pointsAwarded: Int = 0
)

data class MapClickQuizState(
    val stationQueue: List<String>,
    val currentIndex: Int = 0,
    val score: Int = 0,
    val status: GameStatus,
    val currentQuestion: MapClickQuestion?,
    val completedStationIds: List<String> = emptyList()
)

data class GuessResult(
    val state: MapClickQuizState,
    val outcome: GuessOutcome,
    val pointsAwarded: Int
)

/**
 * Click-on-the-map mode: the player keeps tapping until they hit the right station or run out
 * of attempts (Seterra-style - a wrong tap never locks you out, it just costs points and leaves
 * a miss marker, matching the original `quiz-engine.ts` behaviour this was ported from).
 */
object MapClickQuizEngine {

    fun createQuiz(stationIds: List<String>, random: Random = Random.Default): MapClickQuizState {
        val queue = stationIds.shuffled(random)
        return MapClickQuizState(
            stationQueue = queue,
            currentIndex = 0,
            score = 0,
            status = if (queue.isNotEmpty()) GameStatus.PLAYING else GameStatus.COMPLETED,
            currentQuestion = queue.firstOrNull()?.let(::createQuestion),
            completedStationIds = emptyList()
        )
    }

    fun submitStationGuess(state: MapClickQuizState, stationId: String, point: MapPoint): GuessResult {
        val question = state.currentQuestion
        if (question == null || state.status == GameStatus.COMPLETED || question.status != QuestionStatus.GUESSING) {
            return GuessResult(state, GuessOutcome.IGNORED, 0)
        }

        if (stationId == question.stationId) {
            val pointsAwarded = scoreForCorrectGuess(question.missCount)
            val nextQuestion = question.copy(status = QuestionStatus.CORRECT, pointsAwarded = pointsAwarded)
            val nextState = state.copy(score = state.score + pointsAwarded, currentQuestion = nextQuestion)
            return GuessResult(nextState, GuessOutcome.CORRECT, pointsAwarded)
        }

        return registerMiss(state, point)
    }

    fun submitMapMiss(state: MapClickQuizState, point: MapPoint): GuessResult {
        val question = state.currentQuestion
        if (question == null || state.status == GameStatus.COMPLETED || question.status != QuestionStatus.GUESSING) {
            return GuessResult(state, GuessOutcome.IGNORED, 0)
        }
        return registerMiss(state, point)
    }

    fun goToNextQuestion(state: MapClickQuizState): MapClickQuizState {
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
            currentQuestion = createQuestion(nextStationId),
            completedStationIds = completedStationIds
        )
    }

    fun restartQuiz(state: MapClickQuizState, random: Random = Random.Default): MapClickQuizState =
        createQuiz(state.stationQueue, random)

    private fun registerMiss(state: MapClickQuizState, point: MapPoint): GuessResult {
        val question = state.currentQuestion ?: return GuessResult(state, GuessOutcome.IGNORED, 0)

        val missCount = question.missCount + 1
        val missMarkers = question.missMarkers + MissMarker(point.x, point.y, "${question.stationId}-$missCount")
        val shouldReveal = missCount >= MAX_MISSES
        val nextQuestion = question.copy(
            missCount = missCount,
            missMarkers = missMarkers,
            status = if (shouldReveal) QuestionStatus.REVEALED else question.status,
            pointsAwarded = if (shouldReveal) scoreForReveal() else question.pointsAwarded
        )

        return GuessResult(
            state = state.copy(currentQuestion = nextQuestion),
            outcome = if (shouldReveal) GuessOutcome.REVEALED else GuessOutcome.INCORRECT,
            pointsAwarded = 0
        )
    }

    private fun createQuestion(stationId: String) = MapClickQuestion(stationId = stationId)
}
