package com.parismetro.quiz.domain.model

/** The three Seterra-style game modes. */
enum class GameMode {
    /** A station name is given; find and tap it on the map. Multiple attempts allowed. */
    MAP_CLICK,

    /** A station is highlighted; pick its name from a small set of options. One shot. */
    MULTIPLE_CHOICE,

    /** A station is highlighted; type its name. One shot. */
    TYPE_ANSWER
}

/** Which stations a round is drawn from - the "difficulty" axis independent of [GameMode]. */
sealed interface StationScope {
    /** All stations in the network. */
    data object AllStations : StationScope

    /** Only the major interchange stations (2+ lines) - a gentler set for beginners. */
    data object Essentials : StationScope

    /** Only stations served by one of [lineIds] - for drilling specific lines. */
    data class ByLines(val lineIds: Set<String>) : StationScope
}

/**
 * In map-click mode, how a station behaves once it's been found - a difficulty axis on its own,
 * independent of [StationScope]/[GameConfig.showLines]. Ignored by the other two modes.
 */
enum class FoundStationsDisplay {
    /** A found station looks and behaves like any other station (easy to mis-tap again). */
    PLAIN,

    /** A found station stays colored by how many attempts it took, and can't be tapped again. */
    COLORED_LOCKED
}

/** Full configuration for one game session, chosen on the setup screen. */
data class GameConfig(
    val mode: GameMode,
    val scope: StationScope,
    val showLines: Boolean,
    val foundStationsDisplay: FoundStationsDisplay = FoundStationsDisplay.PLAIN
)

/**
 * A stable identifier for a [GameConfig], used as the key under which best scores are stored
 * (so "QCM on line 1" and "QCM on all stations" keep separate high scores).
 */
fun GameConfig.storageKey(): String {
    val scopePart = when (scope) {
        is StationScope.AllStations -> "ALL"
        is StationScope.Essentials -> "ESSENTIALS"
        is StationScope.ByLines -> "LINES:" + scope.lineIds.sorted().joinToString(",")
    }
    return "${mode.name}|$scopePart|lines=${if (showLines) 1 else 0}|found=${foundStationsDisplay.name}"
}
