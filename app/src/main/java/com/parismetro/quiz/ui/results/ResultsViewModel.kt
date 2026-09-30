package com.parismetro.quiz.ui.results

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parismetro.quiz.data.ProgressRepository
import com.parismetro.quiz.domain.model.storageKey
import com.parismetro.quiz.ui.game.GameSessionResult
import kotlinx.coroutines.launch

class ResultsViewModel(
    progressRepository: ProgressRepository,
    result: GameSessionResult
) : ViewModel() {

    var bestScore: Int by mutableIntStateOf(result.score)
        private set

    init {
        viewModelScope.launch {
            // The just-played result is recorded asynchronously by GameViewModel too, so the
            // query below can race it - never show a "best" lower than what was just achieved.
            val stored = progressRepository.bestScore(result.config.storageKey())
            bestScore = maxOf(stored, result.score)
        }
    }
}
