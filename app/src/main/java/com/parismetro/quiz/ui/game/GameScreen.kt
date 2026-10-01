package com.parismetro.quiz.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parismetro.quiz.MetroQuizApplication
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.domain.model.GameMode
import com.parismetro.quiz.ui.ViewModelFactory

/**
 * Hosts one game session: loads the right content composable for [GameConfig.mode] and forwards
 * player actions to [GameViewModel], which owns all the game-state logic.
 */
@Composable
fun GameScreen(
    config: GameConfig,
    onBack: () -> Unit,
    onFinished: (GameSessionResult) -> Unit
) {
    val app = LocalContext.current.applicationContext as MetroQuizApplication
    val viewModel: GameViewModel = viewModel(
        factory = ViewModelFactory { GameViewModel(app.metroRepository, app.progressRepository, config) }
    )

    val state = viewModel.uiState
    LaunchedEffect(state) {
        val finished = state as? GameUiState.Finished ?: return@LaunchedEffect
        onFinished(finished.result)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gameModeTitle(config.mode)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val currentState = state) {
                is GameUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is GameUiState.MapClick -> MapClickContent(
                    state = currentState,
                    onStationTapped = viewModel::onStationTapped,
                    onMapMissed = viewModel::onMapMissed
                )

                is GameUiState.Answer -> when (currentState.gameMode) {
                    GameMode.MULTIPLE_CHOICE -> QcmContent(
                        state = currentState,
                        onOptionSelected = viewModel::onOptionSelected
                    )
                    else -> TypeAnswerContent(
                        state = currentState,
                        onSubmit = viewModel::onTypedAnswerSubmitted
                    )
                }

                is GameUiState.Finished -> Unit // navigation handled by LaunchedEffect above
            }
        }
    }
}

private fun gameModeTitle(mode: GameMode): String = when (mode) {
    GameMode.MAP_CLICK -> "Trouver la station"
    GameMode.MULTIPLE_CHOICE -> "QCM"
    GameMode.TYPE_ANSWER -> "Écrire le nom"
}
