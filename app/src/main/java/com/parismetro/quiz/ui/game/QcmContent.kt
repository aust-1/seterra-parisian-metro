package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.map.MetroMapCanvas
import com.parismetro.quiz.ui.theme.CorrectGreen
import com.parismetro.quiz.ui.theme.IncorrectRed

/** QCM mode: a station is highlighted on the map, pick its name. Locks in on the first tap. */
@Composable
fun QcmContent(
    state: GameUiState.Answer,
    onOptionSelected: (Station) -> Unit
) {
    val answered = state.status != QuestionStatus.GUESSING

    Column(modifier = Modifier.fillMaxSize()) {
        GameHeader(score = state.score, progress = state.progress)
        Box(modifier = Modifier.weight(1f)) {
            MetroMapCanvas(
                stations = state.stations,
                lines = state.lines,
                showLines = state.showLines,
                highlightedStationId = state.highlightedStationId,
                interactive = false,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            "Quelle est cette station ?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.options.orEmpty().forEach { option ->
                val isCorrectOption = option.id == state.highlightedStationId
                val isSelectedWrongOption = answered && !isCorrectOption && option.id == state.selectedStationId
                val colors = when {
                    !answered -> ButtonDefaults.outlinedButtonColors()
                    isCorrectOption -> ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                    isSelectedWrongOption -> ButtonDefaults.buttonColors(containerColor = IncorrectRed)
                    else -> ButtonDefaults.outlinedButtonColors()
                }
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    enabled = !answered,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(option.name)
                }
            }
        }
        if (answered) {
            ResolutionBanner(state.status, state.correctStationName)
        }
    }
}
