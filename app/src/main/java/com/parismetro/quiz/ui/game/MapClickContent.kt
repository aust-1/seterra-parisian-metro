package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.map.MetroMapCanvas
import com.parismetro.quiz.ui.theme.IncorrectRed
import com.parismetro.quiz.ui.theme.MissOrange

/**
 * Map-click mode, Seterra-style: a name is given, tap it on the map.
 * - Correct: the dot flashes green and the game moves on by itself (see [GameViewModel]) - no
 *   button, no delay to click through.
 * - Wrong (attempts remain): the tapped spot is marked and its real name is shown, so a miss is
 *   still a small lesson; the question stays open.
 * - Out of attempts: the correct station blinks red until the player taps it - that tap is what
 *   continues, not a generic "Continuer" button.
 */
@Composable
fun MapClickContent(
    state: GameUiState.MapClick,
    onStationTapped: (Station) -> Unit,
    onMapMissed: (MapPoint) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GameHeader(score = state.score, progress = state.progress)
        Text(
            text = "Trouvez : ${state.targetStationName}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            MetroMapCanvas(
                stations = state.stations,
                lines = state.lines,
                showLines = state.showLines,
                missMarkers = state.missMarkers,
                correctStationId = state.correctStationId,
                revealedStationId = state.revealedStationId,
                interactive = state.status == QuestionStatus.GUESSING || state.status == QuestionStatus.REVEALED,
                onStationTapped = onStationTapped,
                onMapMissed = onMapMissed,
                modifier = Modifier.fillMaxSize()
            )
        }
        MapClickFeedbackLine(state)
    }
}

@Composable
private fun MapClickFeedbackLine(state: GameUiState.MapClick) {
    val feedback: Pair<String, Color>? = when {
        state.status == QuestionStatus.REVEALED ->
            "Clique sur la station qui clignote pour continuer" to IncorrectRed
        state.status == QuestionStatus.GUESSING && state.lastWrongStationName != null ->
            "Tu as cliqué sur : ${state.lastWrongStationName}" to MissOrange
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        feedback?.let { (text, color) ->
            Text(text = text, color = color, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
