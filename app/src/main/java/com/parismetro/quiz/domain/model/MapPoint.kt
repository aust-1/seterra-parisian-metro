package com.parismetro.quiz.domain.model

/** A point in the map's local coordinate space, e.g. where the player tapped. */
data class MapPoint(val x: Float, val y: Float)

/** A recorded wrong tap on the map, so the UI can draw a small "miss" marker there. */
data class MissMarker(val x: Float, val y: Float, val id: String)
