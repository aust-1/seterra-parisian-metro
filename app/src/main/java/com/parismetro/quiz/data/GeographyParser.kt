package com.parismetro.quiz.data

import com.parismetro.quiz.domain.model.MapGeography
import com.parismetro.quiz.domain.model.MapPoint
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * On-disk shape of `assets/metro-data/geography.json`: each ring is a flat
 * `[x0, y0, x1, y1, ...]` array, which keeps the file small (a few thousand points).
 */
@Serializable
private data class GeographyFile(
    val seine: List<List<Float>>,
    val peripherique: List<Float>
)

private val geographyJson = Json { ignoreUnknownKeys = true }

/** Parses `geography.json`. Pure Kotlin, so it is unit-tested without Android. */
fun parseGeography(json: String): MapGeography {
    val file = geographyJson.decodeFromString(GeographyFile.serializer(), json)
    return MapGeography(
        seineRings = file.seine.map(::toPoints).filter { it.size >= 3 },
        peripherique = toPoints(file.peripherique)
    )
}

private fun toPoints(flat: List<Float>): List<MapPoint> {
    require(flat.size % 2 == 0) { "odd number of coordinates in a geography ring" }
    return List(flat.size / 2) { i -> MapPoint(flat[2 * i], flat[2 * i + 1]) }
}
