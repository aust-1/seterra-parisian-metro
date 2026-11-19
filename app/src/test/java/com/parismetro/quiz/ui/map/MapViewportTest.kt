package com.parismetro.quiz.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapViewportTest {

    private val eps = 1e-3f

    @Test
    fun `fitting shows the whole map centred`() {
        val viewport = MapViewport.fitting(1000f, 2000f, mapWidth = 500f, mapHeight = 400f)
        assertEquals(2f, viewport.scale, eps)
        // Map corners land on the view's left/right edges, vertically centred.
        assertEquals(0f, viewport.offsetX(1000f), eps)
        assertEquals((2000f - 400f * 2f) / 2f, viewport.offsetY(2000f), eps)
    }

    @Test
    fun `zooming keeps the map point under the fingers in place`() {
        val viewport = MapViewport(scale = 1f, centerX = 250f, centerY = 200f)
        val (w, h) = 500f to 400f
        // Map point (100, 50) is drawn at screen (100, 50) at scale 1 with this centre.
        val zoomed = viewport.transformed(100f, 50f, panX = 0f, panY = 0f, zoom = 2f, viewWidth = w, viewHeight = h)
        assertEquals(2f, zoomed.scale, eps)
        assertEquals(100f, zoomed.offsetX(w) + 100f * zoomed.scale, eps)
        assertEquals(50f, zoomed.offsetY(h) + 50f * zoomed.scale, eps)
    }

    @Test
    fun `panning moves the centre by the drag distance in map units`() {
        val viewport = MapViewport(scale = 2f, centerX = 300f, centerY = 300f)
        val panned = viewport.transformed(0f, 0f, panX = 40f, panY = -20f, zoom = 1f, viewWidth = 800f, viewHeight = 600f)
        assertEquals(280f, panned.centerX, eps)
        assertEquals(310f, panned.centerY, eps)
    }

    @Test
    fun `zoom stays within its bounds`() {
        val viewport = MapViewport(scale = 1f, centerX = 0f, centerY = 0f)
        assertEquals(MAX_MAP_SCALE, viewport.transformed(0f, 0f, 0f, 0f, 100f, 100f, 100f).scale, eps)
        assertEquals(MIN_MAP_SCALE, viewport.transformed(0f, 0f, 0f, 0f, 0.01f, 100f, 100f).scale, eps)
    }

    @Test
    fun `a size change keeps the same spot in the middle at the same zoom`() {
        // Zoomed in on a neighbourhood in portrait, then the phone is rotated to landscape.
        val zoomedIn = MapViewport.fitting(1080f, 1800f)
            .transformed(300f, 900f, panX = 0f, panY = 0f, zoom = 3f, viewWidth = 1080f, viewHeight = 1800f)
        val landscape = zoomedIn.resized(2200f, 900f)
        assertEquals(zoomedIn, landscape)
        // The map point drawn at the middle of the screen is the same before and after.
        val middleBefore = (1080f / 2f - zoomedIn.offsetX(1080f)) / zoomedIn.scale
        val middleAfter = (2200f / 2f - landscape.offsetX(2200f)) / landscape.scale
        assertEquals(middleBefore, middleAfter, eps)
    }

    @Test
    fun `a map still on its automatic framing is re-fitted to the new size`() {
        val portrait = MapViewport.fitting(1000f, 2000f, mapWidth = 500f, mapHeight = 400f)
        val landscape = portrait.resized(2000f, 1000f)
        assertTrue(landscape.isFit)
        assertEquals(MapViewport.fitting(2000f, 1000f), landscape)
    }

    @Test
    fun `zooming or panning leaves the automatic framing`() {
        val fit = MapViewport.fitting(1000f, 1000f)
        assertFalse(fit.transformed(0f, 0f, 10f, 0f, 1f, 1000f, 1000f).isFit)
    }

    @Test
    fun `the saver round-trips the viewport`() {
        val state = MapViewportState(MapViewport(2.5f, 123f, 456f, isFit = true))
        val saved = with(MapViewportState.Saver) { androidx.compose.runtime.saveable.SaverScope { true }.save(state) }
        val restored = MapViewportState.Saver.restore(saved!!)
        assertEquals(state.viewport, restored!!.viewport)
    }

    @Test
    fun `nothing is saved before the first layout`() {
        val saved = with(MapViewportState.Saver) { androidx.compose.runtime.saveable.SaverScope { true }.save(MapViewportState()) }
        assertEquals(null, saved)
    }
}
