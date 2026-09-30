package com.parismetro.quiz.data

import com.parismetro.quiz.data.db.AppDatabase
import com.parismetro.quiz.data.db.entity.GameResultEntity
import com.parismetro.quiz.data.db.entity.StationStatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local-only progress tracking: best score per [com.parismetro.quiz.domain.model.GameConfig],
 * and per-station stats used to surface the stations a player struggles with most.
 * Everything lives in Room on-device - there is nothing to sync, nothing to upload.
 */
class ProgressRepository(private val db: AppDatabase) {

    suspend fun recordStationResolution(stationId: String, correct: Boolean, missCount: Int = 0) =
        withContext(Dispatchers.IO) {
            val dao = db.stationStatDao()
            val existing = dao.get(stationId) ?: StationStatEntity(stationId = stationId)
            dao.upsert(
                existing.copy(
                    seen = existing.seen + 1,
                    correct = existing.correct + if (correct) 1 else 0,
                    revealed = existing.revealed + if (correct) 0 else 1,
                    misses = existing.misses + missCount
                )
            )
        }

    suspend fun recordGameCompletion(configKey: String, score: Int, totalQuestions: Int) =
        withContext(Dispatchers.IO) {
            db.gameResultDao().insert(
                GameResultEntity(
                    configKey = configKey,
                    score = score,
                    totalQuestions = totalQuestions,
                    playedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun bestScore(configKey: String): Int =
        withContext(Dispatchers.IO) { db.gameResultDao().bestScore(configKey) ?: 0 }

    suspend fun gamesPlayed(configKey: String): Int =
        withContext(Dispatchers.IO) { db.gameResultDao().gamesPlayed(configKey) }

    suspend fun weakestStations(limit: Int = 10): List<StationStatEntity> =
        withContext(Dispatchers.IO) { db.stationStatDao().weakestStations(limit) }

    fun recentResults(limit: Int = 20) = db.gameResultDao().recentResults(limit)
}
