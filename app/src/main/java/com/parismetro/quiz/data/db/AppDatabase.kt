package com.parismetro.quiz.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.parismetro.quiz.data.db.dao.GameResultDao
import com.parismetro.quiz.data.db.dao.StationStatDao
import com.parismetro.quiz.data.db.entity.GameResultEntity
import com.parismetro.quiz.data.db.entity.StationStatEntity

@Database(
    entities = [GameResultEntity::class, StationStatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun stationStatDao(): StationStatDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metro-quiz.db"
                ).build().also { instance = it }
            }
    }
}
