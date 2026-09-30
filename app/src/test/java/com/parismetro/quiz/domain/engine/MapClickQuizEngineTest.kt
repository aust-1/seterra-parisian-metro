package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MapPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class MapClickQuizEngineTest {

    @Test
    fun `creates a shuffled quiz covering every station exactly once`() {
        val quiz = MapClickQuizEngine.createQuiz(listOf("a", "b", "c"), Random(42))

        assertEquals(setOf("a", "b", "c"), quiz.stationQueue.toSet())
        assertEquals(quiz.stationQueue.first(), quiz.currentQuestion?.stationId)
        assertEquals(GameStatus.PLAYING, quiz.status)
    }

    @Test
    fun `awards full points for a correct guess on the first try`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("a", "b", "c"), Random(42))
        val target = quiz.currentQuestion!!.stationId

        val result = MapClickQuizEngine.submitStationGuess(quiz, target, MapPoint(10f, 20f))
        quiz = result.state

        assertEquals(GuessOutcome.CORRECT, result.outcome)
        assertEquals(3, result.pointsAwarded)
        assertEquals(3, quiz.score)
        assertEquals(QuestionStatus.CORRECT, quiz.currentQuestion?.status)
    }

    @Test
    fun `awards fewer points after a miss, and a wrong tap never locks you out`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("a", "b"), Random(1))
        val target = quiz.currentQuestion!!.stationId
        val wrong = quiz.stationQueue.first { it != target }

        val afterMiss = MapClickQuizEngine.submitStationGuess(quiz, wrong, MapPoint(0f, 0f))
        quiz = afterMiss.state
        assertEquals(GuessOutcome.INCORRECT, afterMiss.outcome)
        assertEquals(QuestionStatus.GUESSING, quiz.currentQuestion?.status)

        // still allowed to try again on the same question, per Seterra's re-selection behaviour
        val result = MapClickQuizEngine.submitStationGuess(quiz, target, MapPoint(1f, 1f))

        assertEquals(GuessOutcome.CORRECT, result.outcome)
        assertEquals(2, result.pointsAwarded)
    }

    @Test
    fun `reveals the answer after three misses`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("station"), Random(7))

        quiz = MapClickQuizEngine.submitMapMiss(quiz, MapPoint(1f, 1f)).state
        quiz = MapClickQuizEngine.submitStationGuess(quiz, "wrong-station", MapPoint(2f, 2f)).state
        val result = MapClickQuizEngine.submitMapMiss(quiz, MapPoint(3f, 3f))

        assertEquals(GuessOutcome.REVEALED, result.outcome)
        assertEquals(QuestionStatus.REVEALED, result.state.currentQuestion?.status)
        assertEquals(3, result.state.currentQuestion?.missCount)
        assertEquals(3, result.state.currentQuestion?.missMarkers?.size)
        assertEquals(0, result.state.score)
    }

    @Test
    fun `ignores guesses once a question is resolved`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("a"), Random(3))
        val target = quiz.currentQuestion!!.stationId
        quiz = MapClickQuizEngine.submitStationGuess(quiz, target, MapPoint(0f, 0f)).state

        val result = MapClickQuizEngine.submitStationGuess(quiz, target, MapPoint(0f, 0f))

        assertEquals(GuessOutcome.IGNORED, result.outcome)
    }

    @Test
    fun `moves to the next station only after a resolved question`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("a", "b"), Random(9))
        val firstStation = quiz.currentQuestion?.stationId

        assertEquals(firstStation, MapClickQuizEngine.goToNextQuestion(quiz).currentQuestion?.stationId)

        quiz = MapClickQuizEngine.submitStationGuess(quiz, firstStation!!, MapPoint(0f, 0f)).state
        quiz = MapClickQuizEngine.goToNextQuestion(quiz)

        assertEquals(listOf(firstStation), quiz.completedStationIds)
        assertEquals(GameStatus.PLAYING, quiz.status)
        assertNotEquals(firstStation, quiz.currentQuestion?.stationId)
    }

    @Test
    fun `completes the quiz after the last station is resolved`() {
        var quiz = MapClickQuizEngine.createQuiz(listOf("only"), Random(5))
        val target = quiz.currentQuestion!!.stationId
        quiz = MapClickQuizEngine.submitStationGuess(quiz, target, MapPoint(0f, 0f)).state
        quiz = MapClickQuizEngine.goToNextQuestion(quiz)

        assertEquals(GameStatus.COMPLETED, quiz.status)
        assertNull(quiz.currentQuestion)
    }

    @Test
    fun `an empty station list starts already completed`() {
        val quiz = MapClickQuizEngine.createQuiz(emptyList())

        assertEquals(GameStatus.COMPLETED, quiz.status)
        assertNull(quiz.currentQuestion)
    }
}
