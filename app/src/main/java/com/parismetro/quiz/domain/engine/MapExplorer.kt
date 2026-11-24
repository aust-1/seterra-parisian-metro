package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.StationScope

/**
 * The "Explorer la carte" screen's logic: the same line filter as the game setup (no line
 * selected = the whole network), and what to say about a tapped station.
 */
object MapExplorer {

    fun visibleStations(stations: List<Station>, selectedLineIds: Set<String>): List<Station> =
        if (selectedLineIds.isEmpty()) {
            stations
        } else {
            StationScopeResolver.resolve(StationScope.ByLines(selectedLineIds), stations)
        }

    fun visibleLines(lines: List<MetroLine>, selectedLineIds: Set<String>): List<MetroLine> =
        if (selectedLineIds.isEmpty()) lines else lines.filter { it.id in selectedLineIds }

    /** The lines serving [station], in network order (1, 2, 3, 3bis, …). */
    fun linesOf(station: Station, lines: List<MetroLine>): List<MetroLine> =
        lines.filter { it.id in station.lineIds }

    /** "Correspondance avec les lignes 4, 7, 11 et 14" for interchanges, null otherwise. */
    fun transferSummary(station: Station, lines: List<MetroLine>): String? {
        val labels = linesOf(station, lines).map { it.label }
        if (labels.size < 2) return null
        return "Correspondance entre les lignes " + labels.dropLast(1).joinToString(", ") + " et " + labels.last()
    }
}
