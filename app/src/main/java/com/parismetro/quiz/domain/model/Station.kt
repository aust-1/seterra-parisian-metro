package com.parismetro.quiz.domain.model

import kotlinx.serialization.Serializable

/**
 * A single metro station.
 *
 * [x]/[y] are coordinates in the schematic map's local space (see the `metro-data` assets),
 * not real-world GPS coordinates. The shape matches `assets/metro-data/stations.json` exactly,
 * so this doubles as the deserialization target - no separate DTO needed for data this simple.
 */
@Serializable
data class Station(
    val id: String,
    val name: String,
    val lineIds: List<String>,
    val x: Float,
    val y: Float
)
