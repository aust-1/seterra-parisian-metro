package com.parismetro.quiz

import android.app.Application
import com.parismetro.quiz.data.MetroRepository
import com.parismetro.quiz.data.ProgressRepository
import com.parismetro.quiz.data.db.AppDatabase

/**
 * Hand-rolled DI container: this app is small enough that a full DI framework would add
 * ceremony without buying anything - a couple of lazily-built singletons is enough.
 */
class MetroQuizApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val metroRepository: MetroRepository by lazy { MetroRepository(this) }
    val progressRepository: ProgressRepository by lazy { ProgressRepository(database) }
}
