package com.parismetro.quiz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One completed game session, kept to compute best scores and a simple history. */
@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** [com.parismetro.quiz.domain.model.GameConfig.storageKey] of the session that was played. */
    val configKey: String,
    val score: Int,
    val totalQuestions: Int,
    val playedAt: Long
)
