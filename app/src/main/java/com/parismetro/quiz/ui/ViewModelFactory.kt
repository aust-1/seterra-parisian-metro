package com.parismetro.quiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A tiny generic [ViewModelProvider.Factory]: since this app hand-rolls its DI (see
 * [com.parismetro.quiz.MetroQuizApplication]), each screen just supplies a lambda that builds
 * its ViewModel from the repositories it needs, instead of writing one factory class per screen.
 */
class ViewModelFactory(private val create: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}
