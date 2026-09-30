package com.parismetro.quiz.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.parismetro.quiz.ui.ViewModelFactory

@Composable
fun StatsScreen(onBack: () -> Unit) {
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
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                viewModel.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                viewModel.weakestStations.isEmpty() ->
                    Text(
                        "Pas encore de données - joue quelques parties pour voir tes stations à travailler ici.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> Column {
                    Text("Stations à travailler en priorité", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}
