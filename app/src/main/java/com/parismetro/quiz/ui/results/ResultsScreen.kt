package com.parismetro.quiz.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parismetro.quiz.MetroQuizApplication
import com.parismetro.quiz.domain.engine.MAX_MISSES
import com.parismetro.quiz.ui.ViewModelFactory
import com.parismetro.quiz.ui.game.GameSessionResult

@Composable
fun ResultsScreen(
    result: GameSessionResult,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    val app = LocalContext.current.applicationContext as MetroQuizApplication
    val viewModel: ResultsViewModel = viewModel(
        factory = ViewModelFactory { ResultsViewModel(app.progressRepository, result) }
    )

    val maxPossibleScore = result.totalQuestions * MAX_MISSES

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Partie terminée", style = MaterialTheme.typography.headlineMedium)
        Text("Score : ${result.score} / $maxPossibleScore")
        Text("Meilleur score sur cette configuration : ${viewModel.bestScore}")

        Spacer(Modifier.height(8.dp))

        if (result.missedStationNames.isEmpty()) {
            Text("Sans faute, bravo !", style = MaterialTheme.typography.titleMedium)
        } else {
            Text("Stations à revoir :", style = MaterialTheme.typography.titleMedium)
            result.missedStationNames.forEach { name -> Text("• $name") }
        }

        Spacer(Modifier.weight(1f))

        Button(onClick = onReplay, modifier = Modifier.fillMaxWidth()) { Text("Rejouer") }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Retour à l'accueil") }
    }
}
