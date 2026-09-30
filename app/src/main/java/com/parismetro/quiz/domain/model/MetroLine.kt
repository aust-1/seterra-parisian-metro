package com.parismetro.quiz.domain.model

import kotlinx.serialization.Serializable

/**
 * A metro line, drawn on the map as one or more polylines (a line can fork into branches,
 * e.g. line 7 splitting towards Mairie d'Ivry / Villejuif).
 *
 * [color]/[textColor] are hex strings (e.g. "#FFBE00") as shipped in the data, parsed into
 * Compose Color at the UI boundary rather than here, to keep the domain layer Android-free.
 * The shape matches `assets/metro-data/lines.json` exactly, so this doubles as the
 * deserialization target.
 */
@Serializable
data class MetroLine(
    val id: String,
    val name: String,
    val label: String,
    val color: String,
    val textColor: String,
    val paths: List<List<String>>
)
