package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.map.MetroMapCanvas

/** Map-click mode: a name is given, tap it on the map. Wrong taps just cost points, no lockout. */
@Composable
fun MapClickContent(
    state: GameUiState.MapClick,
    onStationTapped: (Station) -> Unit,
    onMapMissed: (MapPoint) -> Unit,
    onContinue: () -> Unit
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
                correctStationId = state.resolvedStationId,
                interactive = state.status == QuestionStatus.GUESSING,
                onStationTapped = onStationTapped,
                onMapMissed = onMapMissed,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (state.status != QuestionStatus.GUESSING) {
            ResolutionBanner(state.status, state.targetStationName, onContinue)
        }
    }
}
