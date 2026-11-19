package com.parismetro.quiz.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.parismetro.quiz.data.db.dao.GameResultDao
import com.parismetro.quiz.data.db.dao.StationStatDao
import com.parismetro.quiz.data.db.entity.GameResultEntity
import com.parismetro.quiz.data.db.entity.StationStatEntity

@Database(
    entities = [GameResultEntity::class, StationStatEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun stationStatDao(): StationStatDao

    companion object {
        /**
         * v2 adds [StationStatEntity.firstTry], for the per-line first-try rate. It wasn't
         * recorded before, so existing rows get a lower bound: a correct answer counts as a
         * first try unless misses could account for it. Exact for QCM/typed answers (no
         * misses), conservative for map-click ones.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE station_stats ADD COLUMN firstTry INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE station_stats SET firstTry = MAX(0, correct - misses)")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metro-quiz.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
