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
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.map.MetroMapCanvas
import com.parismetro.quiz.ui.theme.IncorrectRed

/**
 * Map-click mode, Seterra-style: a name is given, tap it on the map.
 * - Correct: the dot flashes green and the game moves on by itself (see [GameViewModel]) - no
 *   button, no delay to click through.
 * - Wrong (attempts remain): the tapped spot is marked orange and its real name appears right
 *   next to it on the map, immediately - so a miss is still a small lesson; the question stays
 *   open (see [GameUiState.MapClick.lastWrongStationId]).
 * - Out of attempts: the correct station blinks red until the player taps it - that tap is what
 *   continues, not a generic "Continuer" button.
 * - If the "found stations stay colored" difficulty is on, resolved stations stay green/yellow/
 *   orange/red by attempt count and can't be tapped again (see [GameUiState.MapClick.foundStations]).
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
                foundStations = state.foundStations,
                lastWrongStationId = state.lastWrongStationId,
                interactive = state.status == QuestionStatus.GUESSING || state.status == QuestionStatus.REVEALED,
                onStationTapped = onStationTapped,
                onMapMissed = onMapMissed,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 20.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (state.status == QuestionStatus.REVEALED) {
                Text(
                    text = "Clique sur la station qui clignote pour continuer",
                    color = IncorrectRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
