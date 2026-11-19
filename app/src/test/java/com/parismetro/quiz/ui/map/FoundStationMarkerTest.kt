package com.parismetro.quiz.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.parismetro.quiz.domain.engine.MAX_MISSES
import com.parismetro.quiz.ui.theme.MetroSurfaceDark
import com.parismetro.quiz.ui.theme.MetroSurfaceLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundStationMarkerTest {

    @Test
    fun `every found-station state has its own shape`() {
        val shapes = (0..MAX_MISSES).map(FoundMarkerShape::forMissCount)
        assertEquals(
            listOf(FoundMarkerShape.DOT, FoundMarkerShape.DIAMOND, FoundMarkerShape.TRIANGLE, FoundMarkerShape.CROSS),
            shapes
        )
        assertEquals(shapes.size, shapes.toSet().size)
    }

    @Test
    fun `every state has its own color`() {
        val colors = FoundMarkerShape.entries.map { it.color() }
        assertEquals(colors.size, colors.toSet().size)
    }

    @Test
    fun `each marker color keeps 3 to 1 contrast on the light and the dark map`() {
        FoundMarkerShape.entries.forEach { shape ->
            listOf(MetroSurfaceLight, MetroSurfaceDark).forEach { background ->
                val ratio = contrast(shape.color(), background)
                assertTrue("$shape on $background: $ratio", ratio >= 3f)
            }
        }
    }

    /** WCAG contrast ratio. */
    private fun contrast(a: Color, b: Color): Float {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05f) / (dark + 0.05f)
    }
}
