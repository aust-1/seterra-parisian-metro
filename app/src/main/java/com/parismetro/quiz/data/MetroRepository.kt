package com.parismetro.quiz.data

import android.content.Context
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Loads the bundled metro network (stations + lines) from `assets/metro-data/`.
 *
 * The data never changes at runtime and is small (a few hundred KB), so it's parsed once and
 * cached in memory for the app's lifetime - there is no network fetch and no database table
 * for it, matching this being static reference data rather than user data.
 */
class MetroRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val loadMutex = Mutex()

    @Volatile
    private var cachedStations: List<Station>? = null

    @Volatile
    private var cachedLines: List<MetroLine>? = null

    suspend fun getStations(): List<Station> = cachedStations ?: load().first
    suspend fun getLines(): List<MetroLine> = cachedLines ?: load().second

    /** Guarded by [loadMutex] so two concurrent first-callers don't both parse the assets. */
    private suspend fun load(): Pair<List<Station>, List<MetroLine>> = loadMutex.withLock {
        cachedStations?.let { stations -> return@withLock stations to (cachedLines ?: emptyList()) }

        withContext(Dispatchers.IO) {
            val stations = readAsset("metro-data/stations.json")
                .let { json.decodeFromString<List<Station>>(it) }
            val lines = readAsset("metro-data/lines.json")
                .let { json.decodeFromString<List<MetroLine>>(it) }

            cachedStations = stations
            cachedLines = lines
            stations to lines
        }
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
