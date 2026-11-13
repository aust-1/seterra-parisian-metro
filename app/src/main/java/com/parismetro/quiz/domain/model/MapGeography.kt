package com.parismetro.quiz.domain.model

/**
 * Real geography drawn under the metro network, in the same map-space coordinates as station
 * x/y (see `scripts/generate-geography.py`, which fits that projection on the stations).
 *
 * @property seineRings the Seine's water surface as closed rings: riverbanks wind one way and
 *   islands (Cité, Saint-Louis, aux Cygnes, la Jatte, Saint-Denis...) the other, so a non-zero
 *   fill draws the river splitting around every island.
 * @property peripherique the Boulevard Périphérique as one closed ring.
 */
data class MapGeography(
    val seineRings: List<List<MapPoint>>,
    val peripherique: List<MapPoint>
) {
    companion object {
        /** No geography: the map still works, it just shows the bare network. */
        val EMPTY = MapGeography(seineRings = emptyList(), peripherique = emptyList())
    }
}
