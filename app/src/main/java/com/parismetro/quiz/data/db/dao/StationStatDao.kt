package com.parismetro.quiz.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.parismetro.quiz.data.db.entity.StationStatEntity

@Dao
interface StationStatDao {

    @Query("SELECT * FROM station_stats WHERE stationId = :stationId")
    suspend fun get(stationId: String): StationStatEntity?

    @Upsert
    suspend fun upsert(stat: StationStatEntity)

    /** Stations the player struggles with most, ranked by misses + reveals (most first). */
    @Query(
        """
        SELECT * FROM station_stats
        WHERE seen > 0
        ORDER BY (misses + revealed) DESC, seen DESC
        LIMIT :limit
        """
    )
    suspend fun weakestStations(limit: Int = 10): List<StationStatEntity>
}
