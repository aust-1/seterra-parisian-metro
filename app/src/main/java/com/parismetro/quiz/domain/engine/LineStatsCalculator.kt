package com.parismetro.quiz.domain.engine

import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.domain.model.StationProgress

/** First-try success on one line's stations, all game modes together. */
data class LineStat(
    val line: MetroLine,
    /** Questions asked on this line's stations. */
    val seen: Int,
    /** Of those, answered right on the first try. */
    val firstTry: Int
) {
    /** 0..1, or null if none of this line's stations has been asked yet. */
    val firstTryRate: Float? get() = if (seen == 0) null else firstTry.toFloat() / seen
}

object LineStatsCalculator {

    /**
     * One [LineStat] per line, weakest first: lowest first-try rate first, ties broken by the
     * most questions seen (a sure weakness before a fluke), lines never played last in network
     * order. A station served by several lines counts for each of them.
     */
    fun compute(lines: List<MetroLine>, stations: List<Station>, progress: List<StationProgress>): List<LineStat> {
        val progressById = progress.associateBy { it.stationId }
        val stats = lines.map { line ->
            val lineProgress = stations
                .filter { line.id in it.lineIds }
                .mapNotNull { progressById[it.id] }
            LineStat(line, seen = lineProgress.sumOf { it.seen }, firstTry = lineProgress.sumOf { it.firstTry })
        }
        val (played, unplayed) = stats.partition { it.seen > 0 }
        return played.sortedWith(compareBy<LineStat> { it.firstTryRate }.thenByDescending { it.seen }) + unplayed
    }
}
