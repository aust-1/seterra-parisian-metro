package com.parismetro.quiz.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parismetro.quiz.data.MetroRepository
import com.parismetro.quiz.data.ProgressRepository
import com.parismetro.quiz.domain.engine.AnswerNormalizer
import com.parismetro.quiz.domain.engine.AnswerQuizEngine
import com.parismetro.quiz.domain.engine.AnswerQuizState
import com.parismetro.quiz.domain.engine.DistractorPicker
import com.parismetro.quiz.domain.engine.GameStatus
import com.parismetro.quiz.domain.engine.GuessOutcome
import com.parismetro.quiz.domain.engine.MapClickQuizEngine
import com.parismetro.quiz.domain.engine.MapClickQuizState
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.engine.StationScopeResolver
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.domain.model.GameMode
import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.storageKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val QCM_OPTION_COUNT = 4

// Seterra-style pacing: a correct answer advances almost immediately (just long enough to
// register the green flash); a wrong QCM/type answer lingers a bit longer so the correct name
// can be read. Map-click mode's "revealed" state never auto-advances - see onStationTapped.
private const val CORRECT_ADVANCE_DELAY_MS = 500L
private const val INCORRECT_ADVANCE_DELAY_MS = 1300L

/**
 * Drives one game session. Internally this delegates to [MapClickQuizEngine] or
 * [AnswerQuizEngine] depending on [GameConfig.mode] and republishes their state as one
 * [GameUiState] the screen can render without knowing which engine is behind it.
 */
class GameViewModel(
    private val metroRepository: MetroRepository,
    private val progressRepository: ProgressRepository,
    private val config: GameConfig
) : ViewModel() {

    var uiState: GameUiState by mutableStateOf(GameUiState.Loading)
        private set

    private var allStations: List<Station> = emptyList()
    private var scopedStations: List<Station> = emptyList()
    private var lines: List<MetroLine> = emptyList()

    private var mapClickState: MapClickQuizState? = null
    private var answerState: AnswerQuizState? = null
    private var currentOptions: List<Station> = emptyList()
    private var selectedOptionId: String? = null
    private var lastWrongStationName: String? = null
    private var autoAdvanceJob: Job? = null
    private val missedStationIds = mutableListOf<String>()

    init {
        viewModelScope.launch {
            allStations = metroRepository.getStations()
            lines = metroRepository.getLines()
            scopedStations = StationScopeResolver.resolve(config.scope, allStations)
            val ids = scopedStations.map { it.id }

            if (config.mode == GameMode.MAP_CLICK) {
                mapClickState = MapClickQuizEngine.createQuiz(ids, Random.Default)
                publishMapClickState()
            } else {
                answerState = AnswerQuizEngine.createQuiz(ids, Random.Default)
                refreshOptionsForCurrentQuestion()
                publishAnswerState()
            }
        }
    }

    // ----- Map-click mode -----

    fun onStationTapped(station: Station) {
        val state = mapClickState ?: return
        val question = state.currentQuestion ?: return

        if (question.status == QuestionStatus.REVEALED) {
            // Once the answer is revealed, only tapping that exact station moves things on -
            // Seterra's "blinks until you click it" acknowledgement, not a generic button.
            if (station.id == question.stationId) onContinue()
            return
        }

        lastWrongStationName = station.id.takeIf { it != question.stationId }?.let { station.name }
        val result = MapClickQuizEngine.submitStationGuess(state, station.id, MapPoint(station.x, station.y))
        mapClickState = result.state
        publishMapClickState()
        if (result.outcome == GuessOutcome.CORRECT) scheduleAutoAdvance(CORRECT_ADVANCE_DELAY_MS)
    }

    fun onMapMissed(point: MapPoint) {
        val state = mapClickState ?: return
        val question = state.currentQuestion ?: return
        if (question.status == QuestionStatus.REVEALED) return

        lastWrongStationName = null
        mapClickState = MapClickQuizEngine.submitMapMiss(state, point).state
        publishMapClickState()
    }

    // ----- QCM / type-answer modes -----

    fun onOptionSelected(station: Station) {
        val question = answerState?.currentQuestion ?: return
        selectedOptionId = station.id
        submitAnswer(isCorrect = station.id == question.stationId)
    }

    fun onTypedAnswerSubmitted(text: String) {
        val question = answerState?.currentQuestion ?: return
        val target = scopedStations.firstOrNull { it.id == question.stationId } ?: return
        submitAnswer(isCorrect = AnswerNormalizer.matches(text, target.name))
    }

    private fun submitAnswer(isCorrect: Boolean) {
        val state = answerState ?: return
        answerState = AnswerQuizEngine.submitAnswer(state, isCorrect).state
        publishAnswerState()
        scheduleAutoAdvance(if (isCorrect) CORRECT_ADVANCE_DELAY_MS else INCORRECT_ADVANCE_DELAY_MS)
    }

    // ----- Shared -----

    /** Advances to the next question. Auto-scheduled after a delay - see [scheduleAutoAdvance] -
     * except for map-click's REVEALED state, which instead calls this directly from
     * [onStationTapped] once the player taps the (blinking) correct station. */
    fun onContinue() {
        if (config.mode == GameMode.MAP_CLICK) {
            val state = mapClickState ?: return
            val question = state.currentQuestion ?: return
            if (question.status == QuestionStatus.GUESSING) return

            recordResolution(question.stationId, question.status == QuestionStatus.CORRECT, question.missCount)
            lastWrongStationName = null
            val next = MapClickQuizEngine.goToNextQuestion(state)
            mapClickState = next
            if (next.status == GameStatus.COMPLETED) finishSession(next.score, next.stationQueue.size) else publishMapClickState()
        } else {
            val state = answerState ?: return
            val question = state.currentQuestion ?: return
            if (question.status == QuestionStatus.GUESSING) return

            recordResolution(question.stationId, question.status == QuestionStatus.CORRECT, missCount = 0)
            val next = AnswerQuizEngine.goToNextQuestion(state)
            answerState = next
            if (next.status == GameStatus.COMPLETED) {
                finishSession(next.score, next.stationQueue.size)
            } else {
                refreshOptionsForCurrentQuestion()
                publishAnswerState()
            }
        }
    }

    /** Replaces an explicit "Continuer" button: resolve, show color feedback, move on by itself. */
    private fun scheduleAutoAdvance(delayMs: Long) {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            delay(delayMs)
            onContinue()
        }
    }

    private fun refreshOptionsForCurrentQuestion() {
        selectedOptionId = null
        if (config.mode != GameMode.MULTIPLE_CHOICE) return
        val question = answerState?.currentQuestion ?: return
        val correctStation = scopedStations.firstOrNull { it.id == question.stationId } ?: return
        currentOptions = DistractorPicker.pickOptions(correctStation, scopedStations, QCM_OPTION_COUNT, Random.Default)
    }

    private fun recordResolution(stationId: String, correct: Boolean, missCount: Int) {
        if (!correct) missedStationIds += stationId
        viewModelScope.launch {
            progressRepository.recordStationResolution(stationId, correct, missCount)
        }
    }

    private fun finishSession(score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            progressRepository.recordGameCompletion(config.storageKey(), score, totalQuestions)
        }
        val missedNames = missedStationIds.distinct()
            .mapNotNull { id -> allStations.firstOrNull { it.id == id }?.name }
        uiState = GameUiState.Finished(GameSessionResult(config, score, totalQuestions, missedNames))
    }

    private fun publishMapClickState() {
        val state = mapClickState ?: return
        val question = state.currentQuestion ?: return
        val target = scopedStations.firstOrNull { it.id == question.stationId } ?: return

        uiState = GameUiState.MapClick(
            stations = scopedStations,
            lines = lines,
            showLines = config.showLines,
            targetStationName = target.name,
            missMarkers = question.missMarkers,
            status = question.status,
            correctStationId = if (question.status == QuestionStatus.CORRECT) question.stationId else null,
            revealedStationId = if (question.status == QuestionStatus.REVEALED) question.stationId else null,
            lastWrongStationName = lastWrongStationName,
            score = state.score,
            progress = (state.currentIndex + 1) to state.stationQueue.size
        )
    }

    private fun publishAnswerState() {
        val state = answerState ?: return
        val question = state.currentQuestion ?: return
        val target = scopedStations.firstOrNull { it.id == question.stationId } ?: return

        uiState = GameUiState.Answer(
            gameMode = config.mode,
            stations = scopedStations,
            lines = lines,
            showLines = config.showLines,
            highlightedStationId = target.id,
            options = if (config.mode == GameMode.MULTIPLE_CHOICE) currentOptions else null,
            selectedStationId = selectedOptionId,
            status = question.status,
            correctStationName = target.name,
            score = state.score,
            progress = (state.currentIndex + 1) to state.stationQueue.size
        )
    }
}
