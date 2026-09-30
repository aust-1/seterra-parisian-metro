package com.parismetro.quiz.domain.model

/**
 * A metro line, drawn on the map as one or more polylines (a line can fork into branches,
 * e.g. line 7 splitting towards Mairie d'Ivry / Villejuif).
 *
 * [color]/[textColor] are hex strings (e.g. "#FFBE00") as shipped in the data, parsed into
 * Compose Color at the UI boundary rather than here, to keep the domain layer Android-free.
 */
data class MetroLine(
    val id: String,
    val name: String,
    val label: String,
    val color: String,
    val textColor: String,
    val paths: List<List<String>>
)
