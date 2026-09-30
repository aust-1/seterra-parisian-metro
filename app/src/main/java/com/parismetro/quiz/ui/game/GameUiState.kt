package com.parismetro.quiz.ui.game

import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.domain.model.GameMode
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.MissMarker
import com.parismetro.quiz.domain.model.Station

/** Everything [com.parismetro.quiz.ui.game.GameScreen] needs to render, for either engine. */
sealed interface GameUiState {

    data object Loading : GameUiState

    data class MapClick(
        val stations: List<Station>,
        val lines: List<MetroLine>,
        val showLines: Boolean,
        val targetStationName: String,
        val missMarkers: List<MissMarker>,
        val status: QuestionStatus,
        /** The target station's id once [status] is no longer GUESSING, else null. */
        val resolvedStationId: String?,
        val score: Int,
        val progress: Pair<Int, Int>
    ) : GameUiState

    data class Answer(
        val gameMode: GameMode,
        val stations: List<Station>,
        val lines: List<MetroLine>,
        val showLines: Boolean,
        val highlightedStationId: String,
        /** Only populated for [GameMode.MULTIPLE_CHOICE]. */
        val options: List<Station>?,
        /** The option the player tapped (MULTIPLE_CHOICE only), so only it is marked wrong. */
        val selectedStationId: String? = null,
        val status: QuestionStatus,
        val correctStationName: String,
        val score: Int,
        val progress: Pair<Int, Int>
    ) : GameUiState

    data class Finished(val result: GameSessionResult) : GameUiState
}

/** Summary handed to the results screen once a session's last question is resolved. */
data class GameSessionResult(
    val config: GameConfig,
    val score: Int,
    val totalQuestions: Int,
    val missedStationNames: List<String>
)
