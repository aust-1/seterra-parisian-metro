package com.parismetro.quiz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.parismetro.quiz.data.db.entity.GameResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {

    @Insert
    suspend fun insert(result: GameResultEntity)

    @Query("SELECT MAX(score) FROM game_results WHERE configKey = :configKey")
    suspend fun bestScore(configKey: String): Int?

    @Query("SELECT COUNT(*) FROM game_results WHERE configKey = :configKey")
    suspend fun gamesPlayed(configKey: String): Int

    @Query("SELECT * FROM game_results ORDER BY playedAt DESC LIMIT :limit")
    fun recentResults(limit: Int = 20): Flow<List<GameResultEntity>>
}
