package com.parismetro.quiz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Lifetime stats for one station, across every game mode, used to surface weak spots. */
@Entity(tableName = "station_stats")
data class StationStatEntity(
    @PrimaryKey
    val stationId: String,
    val seen: Int = 0,
    val correct: Int = 0,
    val revealed: Int = 0,
    val misses: Int = 0
)
