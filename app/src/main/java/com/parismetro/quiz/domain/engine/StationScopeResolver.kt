package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.StationScope

/** Resolves a [StationScope] (a difficulty setting) into the concrete stations it covers. */
object StationScopeResolver {

    /** A station counts as a major interchange ([StationScope.Essentials]) from 2 lines up. */
    private const val ESSENTIALS_MIN_LINES = 2

    fun resolve(scope: StationScope, stations: List<Station>): List<Station> = when (scope) {
        is StationScope.AllStations -> stations
        is StationScope.Essentials -> stations.filter { it.lineIds.size >= ESSENTIALS_MIN_LINES }
        is StationScope.ByLines -> stations.filter { station ->
            station.lineIds.any { it in scope.lineIds }
        }
    }
}
