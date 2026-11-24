package com.parismetro.quiz.ui.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parismetro.quiz.data.MetroRepository
import com.parismetro.quiz.domain.engine.MapExplorer
import com.parismetro.quiz.domain.model.MapGeography
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import kotlinx.coroutines.launch

/** Study mode: the whole map with names, filterable by line, tap a station for its details. */
class ExploreViewModel(metroRepository: MetroRepository) : ViewModel() {

    var isLoading: Boolean by mutableStateOf(true)
        private set

    var allLines: List<MetroLine> by mutableStateOf(emptyList())
        private set

    var geography: MapGeography by mutableStateOf(MapGeography.EMPTY)
        private set

    /** Same filter as the game setup's "Par ligne": empty means the whole network. */
    var selectedLineIds: Set<String> by mutableStateOf(emptySet())
        private set

    var selectedStation: Station? by mutableStateOf(null)
        private set

    private var allStations: List<Station> by mutableStateOf(emptyList())

    val visibleStations: List<Station> get() = MapExplorer.visibleStations(allStations, selectedLineIds)
    val visibleLines: List<MetroLine> get() = MapExplorer.visibleLines(allLines, selectedLineIds)

    init {
        viewModelScope.launch {
            allStations = metroRepository.getStations()
            allLines = metroRepository.getLines()
            geography = metroRepository.getGeography()
            isLoading = false
        }
    }

    fun onLineToggled(lineId: String) {
        selectedLineIds = if (lineId in selectedLineIds) selectedLineIds - lineId else selectedLineIds + lineId
        if (selectedStation?.let { it in visibleStations } == false) selectedStation = null
    }

    fun onAllLinesSelected() {
        selectedLineIds = emptySet()
    }

    fun onStationTapped(station: Station) {
        selectedStation = station
    }

    fun onMapTappedElsewhere() {
        selectedStation = null
    }
}
