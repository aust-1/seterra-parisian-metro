package com.parismetro.quiz.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parismetro.quiz.MetroQuizApplication
import com.parismetro.quiz.domain.engine.LineStat
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.domain.model.GameMode
import com.parismetro.quiz.domain.model.StationScope
import com.parismetro.quiz.ui.ViewModelFactory
import com.parismetro.quiz.ui.map.LineBadge
import com.parismetro.quiz.ui.map.parseHexColor
import kotlin.math.roundToInt

/**
 * @param onPractice starts a game on one line's stations - the "S'entraîner" button of the
 *   per-line stats.
 */
@Composable
fun StatsScreen(onBack: () -> Unit, onPractice: (GameConfig) -> Unit) {
    val app = LocalContext.current.applicationContext as MetroQuizApplication
    val viewModel: StatsViewModel = viewModel(
        factory = ViewModelFactory { StatsViewModel(app.metroRepository, app.progressRepository) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            when {
                viewModel.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                viewModel.weakestStations.isEmpty() ->
                    Text(
                        "Pas encore de données - joue quelques parties pour voir tes stations à travailler ici.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        SectionTitle("Réussite au premier essai, par ligne")
                    }
                    items(viewModel.lineStats, key = { "line-" + it.line.id }) { stat ->
                        LineStatRow(stat, onPractice = {
                            onPractice(
                                GameConfig(
                                    mode = GameMode.MAP_CLICK,
                                    scope = StationScope.ByLines(setOf(stat.line.id)),
                                    showLines = true
                                )
                            )
                        })
                    }
                    item {
                        SectionTitle("Stations à travailler en priorité")
                    }
                    items(viewModel.weakestStations) { stat ->
                        ListItem(
                            headlineContent = { Text(stat.name) },
                            supportingContent = {
                                Text("Vue ${stat.seen} fois - ${stat.misses + stat.revealed} erreur(s)")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
}

/** A line's badge, its first-try rate as a bar in the line's color, and "S'entraîner". */
@Composable
private fun LineStatRow(stat: LineStat, onPractice: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LineBadge(stat.line)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val rate = stat.firstTryRate
            if (rate == null) {
                Text("Pas encore jouée", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    "${(rate * 100).roundToInt()} % au premier essai (${stat.firstTry} / ${stat.seen})",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = { rate },
                    color = parseHexColor(stat.line.color),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        OutlinedButton(onClick = onPractice) { Text("S'entraîner") }
    }
}
