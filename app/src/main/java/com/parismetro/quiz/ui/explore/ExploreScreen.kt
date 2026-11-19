package com.parismetro.quiz.ui.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.parismetro.quiz.domain.engine.MapExplorer
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.ViewModelFactory
import com.parismetro.quiz.ui.map.LineBadge
import com.parismetro.quiz.ui.map.MetroMapCanvas
import com.parismetro.quiz.ui.setup.LineMultiSelector

/**
 * "Explorer la carte": study the map before playing. Every station is named once zoomed in
 * enough to read; tapping one shows its name, its lines and its transfers. The line filter is
 * the game setup's "Par ligne" one, so a line can be studied on its own before being drilled.
 */
@Composable
fun ExploreScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as MetroQuizApplication
    val viewModel: ExploreViewModel = viewModel(
        factory = ViewModelFactory { ExploreViewModel(app.metroRepository) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorer la carte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.selectedLineIds.isEmpty(),
                        onClick = viewModel::onAllLinesSelected,
                        label = { Text("Toutes") }
                    )
                    LineMultiSelector(
                        lines = viewModel.allLines,
                        selected = viewModel.selectedLineIds,
                        onToggle = viewModel::onLineToggled
                    )
                }
                Text(
                    "Zoome pour lire les noms, touche une station pour le détail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    MetroMapCanvas(
                        stations = viewModel.visibleStations,
                        lines = viewModel.visibleLines,
                        geography = viewModel.geography,
                        highlightedStationId = viewModel.selectedStation?.id,
                        showStationNames = true,
                        interactive = true,
                        onStationTapped = viewModel::onStationTapped,
                        onMapMissed = { viewModel.onMapTappedElsewhere() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                viewModel.selectedStation?.let { station ->
                    StationDetails(station, viewModel.allLines, Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StationDetails(station: Station, lines: List<MetroLine>, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(station.name, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MapExplorer.linesOf(station, lines).forEach { LineBadge(it) }
            }
            Text(
                MapExplorer.transferSummary(station, lines) ?: "Pas de correspondance avec une autre ligne",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
