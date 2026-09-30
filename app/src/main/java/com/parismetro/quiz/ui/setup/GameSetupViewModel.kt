package com.parismetro.quiz.ui.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parismetro.quiz.data.MetroRepository
import com.parismetro.quiz.domain.model.MetroLine
import kotlinx.coroutines.launch

/** Just loads the line list so the setup screen can offer "revise this specific line". */
class GameSetupViewModel(metroRepository: MetroRepository) : ViewModel() {

    var lines: List<MetroLine> by mutableStateOf(emptyList())
        private set

    init {
        viewModelScope.launch { lines = metroRepository.getLines() }
    }
}
