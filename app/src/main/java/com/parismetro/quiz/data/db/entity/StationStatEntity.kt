package com.parismetro.quiz.data.db.entity

import androidx.room.ColumnInfo
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
    val misses: Int = 0,
    /** Answered right with no miss before (always the case for a correct QCM/typed answer). */
    @ColumnInfo(defaultValue = "0")
    val firstTry: Int = 0
)
