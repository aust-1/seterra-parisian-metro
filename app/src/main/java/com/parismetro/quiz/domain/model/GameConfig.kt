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

/** Full configuration for one game session, chosen on the setup screen. */
data class GameConfig(
    val mode: GameMode,
    val scope: StationScope,
    val showLines: Boolean
)
