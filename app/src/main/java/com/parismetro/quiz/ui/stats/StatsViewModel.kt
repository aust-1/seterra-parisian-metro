package com.parismetro.quiz.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parismetro.quiz.data.MetroRepository
import com.parismetro.quiz.data.ProgressRepository
import com.parismetro.quiz.domain.engine.LineStat
import com.parismetro.quiz.domain.engine.LineStatsCalculator
import kotlinx.coroutines.launch

data class WeakStationUi(val name: String, val seen: Int, val misses: Int, val revealed: Int)

class StatsViewModel(
    private val metroRepository: MetroRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    var isLoading: Boolean by mutableStateOf(true)
        private set

    var weakestStations: List<WeakStationUi> by mutableStateOf(emptyList())
        private set

    /** Every line, weakest first-try rate first, never-played lines last. */
    var lineStats: List<LineStat> by mutableStateOf(emptyList())
        private set

    init {
        viewModelScope.launch {
            val stats = progressRepository.weakestStations(limit = 15)
            val stations = metroRepository.getStations()
            val stationsById = stations.associateBy { it.id }
            weakestStations = stats.mapNotNull { stat ->
                stationsById[stat.stationId]?.let { station ->
                    WeakStationUi(station.name, stat.seen, stat.misses, stat.revealed)
                }
            }
            lineStats = LineStatsCalculator.compute(
                lines = metroRepository.getLines(),
                stations = stations,
                progress = progressRepository.stationProgress()
            )
            isLoading = false
        }
    }
}
