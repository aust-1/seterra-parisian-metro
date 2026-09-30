package com.parismetro.quiz.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class AnswerQuizEngineTest {

    @Test
    fun `creates a shuffled quiz covering every station exactly once`() {
        val quiz = AnswerQuizEngine.createQuiz(listOf("a", "b", "c"), Random(11))

        assertEquals(setOf("a", "b", "c"), quiz.stationQueue.toSet())
        assertEquals(quiz.stationQueue.first(), quiz.currentQuestion?.stationId)
    }

    @Test
    fun `a correct answer locks in immediately and awards full points`() {
        var quiz = AnswerQuizEngine.createQuiz(listOf("a", "b"), Random(2))

        val result = AnswerQuizEngine.submitAnswer(quiz, isCorrect = true)
        quiz = result.state

        assertEquals(GuessOutcome.CORRECT, result.outcome)
        assertEquals(3, result.pointsAwarded)
        assertEquals(QuestionStatus.CORRECT, quiz.currentQuestion?.status)
    }

    @Test
    fun `a wrong answer locks in immediately and awards nothing`() {
        var quiz = AnswerQuizEngine.createQuiz(listOf("a", "b"), Random(2))

        val result = AnswerQuizEngine.submitAnswer(quiz, isCorrect = false)
        quiz = result.state

        assertEquals(GuessOutcome.INCORRECT, result.outcome)
        assertEquals(0, result.pointsAwarded)
        assertEquals(QuestionStatus.REVEALED, quiz.currentQuestion?.status)
    }

    @Test
    fun `ignores a second answer for the same question`() {
        var quiz = AnswerQuizEngine.createQuiz(listOf("a"), Random(4))
        quiz = AnswerQuizEngine.submitAnswer(quiz, isCorrect = true).state

        val result = AnswerQuizEngine.submitAnswer(quiz, isCorrect = true)

        assertEquals(GuessOutcome.IGNORED, result.outcome)
    }

    @Test
    fun `moves to the next station only after answering`() {
        var quiz = AnswerQuizEngine.createQuiz(listOf("a", "b"), Random(6))
        val firstStation = quiz.currentQuestion?.stationId

        assertEquals(firstStation, AnswerQuizEngine.goToNextQuestion(quiz).currentQuestion?.stationId)

        quiz = AnswerQuizEngine.submitAnswer(quiz, isCorrect = true).state
        quiz = AnswerQuizEngine.goToNextQuestion(quiz)

        assertEquals(listOf(firstStation), quiz.completedStationIds)
        assertEquals(GameStatus.PLAYING, quiz.status)
    }

    @Test
    fun `completes the quiz after the last station is answered`() {
        var quiz = AnswerQuizEngine.createQuiz(listOf("only"), Random(8))
        quiz = AnswerQuizEngine.submitAnswer(quiz, isCorrect = true).state
        quiz = AnswerQuizEngine.goToNextQuestion(quiz)

        assertEquals(GameStatus.COMPLETED, quiz.status)
        assertNull(quiz.currentQuestion)
    }
}
