package com.parismetro.quiz.ui.setup

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parismetro.quiz.MetroQuizApplication
import com.parismetro.quiz.domain.model.FoundStationsDisplay
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.domain.model.GameMode
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.StationScope
import com.parismetro.quiz.ui.ViewModelFactory

private enum class ScopeChoice { ALL, ESSENTIALS, BY_LINES }

@Composable
fun GameSetupScreen(onBack: () -> Unit, onStart: (GameConfig) -> Unit) {
    val app = LocalContext.current.applicationContext as MetroQuizApplication
    val viewModel: GameSetupViewModel = viewModel(
        factory = ViewModelFactory { GameSetupViewModel(app.metroRepository) }
    )

    var mode by rememberSaveable { mutableStateOf(GameMode.MAP_CLICK) }
    var scopeChoice by rememberSaveable { mutableStateOf(ScopeChoice.ALL) }
    var selectedLineIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showLines by rememberSaveable { mutableStateOf(true) }
    var foundStationsDisplay by rememberSaveable { mutableStateOf(FoundStationsDisplay.PLAIN) }

    val canStart = scopeChoice != ScopeChoice.BY_LINES || selectedLineIds.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurer la partie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Section("Mode de jeu") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(mode == GameMode.MAP_CLICK, { mode = GameMode.MAP_CLICK }, { Text("Cliquer sur la carte") })
                    FilterChip(mode == GameMode.MULTIPLE_CHOICE, { mode = GameMode.MULTIPLE_CHOICE }, { Text("QCM") })
                    FilterChip(mode == GameMode.TYPE_ANSWER, { mode = GameMode.TYPE_ANSWER }, { Text("Taper le nom") })
                }
            }

            Section("Stations à réviser") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(scopeChoice == ScopeChoice.ALL, { scopeChoice = ScopeChoice.ALL }, { Text("Toutes (305)") })
                    FilterChip(scopeChoice == ScopeChoice.ESSENTIALS, { scopeChoice = ScopeChoice.ESSENTIALS }, { Text("Essentielles") })
                    FilterChip(scopeChoice == ScopeChoice.BY_LINES, { scopeChoice = ScopeChoice.BY_LINES }, { Text("Par ligne") })
                }
                if (scopeChoice == ScopeChoice.BY_LINES) {
                    LineMultiSelector(
                        lines = viewModel.lines,
                        selected = selectedLineIds,
                        onToggle = { id -> selectedLineIds = if (id in selectedLineIds) selectedLineIds - id else selectedLineIds + id }
                    )
                }
            }

            Section("Affichage de la carte") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = showLines, onCheckedChange = { showLines = it })
                    Spacer(Modifier.width(8.dp))
                    Text(if (showLines) "Lignes visibles" else "Lignes masquées (plus difficile)")
                }
            }

            if (mode == GameMode.MAP_CLICK) {
                Section("Stations déjà trouvées") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            foundStationsDisplay == FoundStationsDisplay.PLAIN,
                            { foundStationsDisplay = FoundStationsDisplay.PLAIN },
                            { Text("Restent cliquables") }
                        )
                        FilterChip(
                            foundStationsDisplay == FoundStationsDisplay.COLORED_LOCKED,
                            { foundStationsDisplay = FoundStationsDisplay.COLORED_LOCKED },
                            { Text("Restent colorées, verrouillées") }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val scope = when (scopeChoice) {
                        ScopeChoice.ALL -> StationScope.AllStations
                        ScopeChoice.ESSENTIALS -> StationScope.Essentials
                        ScopeChoice.BY_LINES -> StationScope.ByLines(selectedLineIds)
                    }
                    onStart(GameConfig(mode, scope, showLines, foundStationsDisplay))
                },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Commencer")
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun LineMultiSelector(lines: List<MetroLine>, selected: Set<String>, onToggle: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { line ->
            FilterChip(
                selected = line.id in selected,
                onClick = { onToggle(line.id) },
                label = { Text(line.label) }
            )
        }
    }
}
