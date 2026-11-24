package com.parismetro.quiz.data

import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.map.MAP_HEIGHT
import com.parismetro.quiz.ui.map.MAP_WIDTH
import java.io.File
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The bundled map must keep real proportions: one kilometre is the same number of map units
 * whichever way it points. A per-axis normalization once made the map ~70 % too wide.
 */
class MapProjectionTest {

    private val stations by lazy {
        val file = listOf("src/main/assets/metro-data/stations.json", "app/src/main/assets/metro-data/stations.json")
            .map(::File)
            .first { it.exists() }
        Json { ignoreUnknownKeys = true }.decodeFromString<List<Station>>(file.readText()).associateBy { it.id }
    }

    /** Real positions (OpenStreetMap, lat/lon) of stations at the four corners of the network. */
    private val landmarks = mapOf(
        "pont-de-sevres" to (48.82967 to 2.23084),
        "chateau-de-vincennes" to (48.84458 to 2.43891),
        "saint-denis-universite" to (48.94587 to 2.36465),
        "villejuif-louis-aragon" to (48.78694 to 2.36739),
        "pont-de-levallois-becon" to (48.89715 to 2.28041),
        "mairie-de-montreuil" to (48.86260 to 2.44157),
        "chatelet" to (48.85878 to 2.34741)
    )

    @Test
    fun `the map has the same scale in every direction`() {
        val ids = landmarks.keys.toList()
        val unitsPerKm = ids.flatMap { a -> ids.filter { it > a }.map { b -> a to b } }.map { (a, b) ->
            val sa = stations.getValue(a)
            val sb = stations.getValue(b)
            val km = haversineKm(landmarks.getValue(a), landmarks.getValue(b))
            Triple(a, b, hypot(sa.x - sb.x, sa.y - sb.y) / km)
        }
        val median = unitsPerKm.map { it.third }.sorted()[unitsPerKm.size / 2]
        unitsPerKm.forEach { (a, b, scale) ->
            assertTrue(
                "$a - $b : $scale unités/km, médiane $median",
                scale in median * 0.95..median * 1.05
            )
        }
    }

    @Test
    fun `every station lies inside the map frame`() {
        stations.values.forEach { s ->
            assertTrue("${s.name} (${s.x}, ${s.y})", s.x in 0f..MAP_WIDTH && s.y in 0f..MAP_HEIGHT)
        }
    }

    private fun haversineKm(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
        val (lat1, lon1) = Math.toRadians(a.first) to Math.toRadians(a.second)
        val (lat2, lon2) = Math.toRadians(b.first) to Math.toRadians(b.second)
        val h = sin((lat2 - lat1) / 2).let { it * it } +
            cos(lat1) * cos(lat2) * sin((lon2 - lon1) / 2).let { it * it }
        return 2 * 6371.0 * asin(sqrt(h))
    }
}
