package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.QuestionStatus
import com.parismetro.quiz.ui.map.MetroMapCanvas

/** Type-the-name mode: a station is highlighted on the map, type its name. Locks in on submit. */
@Composable
fun TypeAnswerContent(
    state: GameUiState.Answer,
    onSubmit: (String) -> Unit
) {
    var text by rememberSaveable(state.progress) { mutableStateOf("") }
    val answered = state.status != QuestionStatus.GUESSING
    val focusManager = LocalFocusManager.current

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
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nom de la station") },
                singleLine = true,
                enabled = !answered,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (!answered && text.isNotBlank()) {
                        onSubmit(text)
                        focusManager.clearFocus()
                    }
                })
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onSubmit(text) },
                enabled = !answered && text.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider")
            }
        }
        if (answered) {
            ResolutionBanner(state.status, state.correctStationName)
        }
    }
}
