package com.parismetro.quiz.domain.model

/**
 * A single metro station.
 *
 * [x]/[y] are coordinates in the schematic map's local space (see the `metro-data` assets),
 * not real-world GPS coordinates.
 */
data class Station(
    val id: String,
    val name: String,
    val lineIds: List<String>,
    val x: Float,
    val y: Float
)
