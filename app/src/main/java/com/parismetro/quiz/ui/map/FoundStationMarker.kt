package com.parismetro.quiz.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.parismetro.quiz.domain.engine.MAX_MISSES
import com.parismetro.quiz.ui.theme.FOUND_STATION_COLORS

/**
 * How a found station is drawn in map-click mode with the "colored" option, by miss count.
 * Each state has its own shape as well as its own color, so the four states stay apart without
 * relying on color (deuteranopia/protanopia make green, yellow and orange hard to tell apart).
 */
enum class FoundMarkerShape(val label: String) {
    /** Found on the first try: a filled dot. */
    DOT("Du 1er coup"),

    /** Found after one miss: a diamond. */
    DIAMOND("1 erreur"),

    /** Found after two misses: a triangle. */
    TRIANGLE("2 erreurs"),

    /** Never found, the answer was revealed: a cross. */
    CROSS("Révélée");

    companion object {
        /** [missCount] as stored in the game state: 0..2, or [MAX_MISSES] for a reveal. */
        fun forMissCount(missCount: Int): FoundMarkerShape = when {
            missCount <= 0 -> DOT
            missCount >= MAX_MISSES -> CROSS
            missCount == 1 -> DIAMOND
            else -> TRIANGLE
        }
    }
}

fun FoundMarkerShape.color(): Color = FOUND_STATION_COLORS[ordinal]

/** A found-station marker's size for a station whose plain dot is [dotRadius] pixels: a bit
 * larger, so the shape reads, and never below 5 dp. */
fun DrawScope.foundMarkerRadius(dotRadius: Float): Float = maxOf(dotRadius * 1.4f, 5.dp.toPx())

/** Draws [shape] centered on [center], fitting in a circle of [radius]. */
fun DrawScope.drawFoundStationMarker(shape: FoundMarkerShape, center: Offset, radius: Float) {
    val color = shape.color()
    when (shape) {
        FoundMarkerShape.DOT -> drawCircle(color = color, radius = radius * 0.85f, center = center)
        FoundMarkerShape.DIAMOND -> drawPath(
            Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius, center.y)
                lineTo(center.x, center.y + radius)
                lineTo(center.x - radius, center.y)
                close()
            },
            color = color
        )
        FoundMarkerShape.TRIANGLE -> drawPath(
            Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius * 0.95f, center.y + radius * 0.75f)
                lineTo(center.x - radius * 0.95f, center.y + radius * 0.75f)
                close()
            },
            color = color
        )
        FoundMarkerShape.CROSS -> {
            val arm = radius * 0.8f
            val stroke = Stroke(width = radius * 0.55f, cap = StrokeCap.Round)
            drawLine(color, center + Offset(-arm, -arm), center + Offset(arm, arm), stroke.width, stroke.cap)
            drawLine(color, center + Offset(-arm, arm), center + Offset(arm, -arm), stroke.width, stroke.cap)
        }
    }
}

/** One-line key to the found-station markers, shown under the map when they're in use. */
@Composable
fun FoundStationsLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FoundMarkerShape.entries.forEach { shape ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(Modifier.size(12.dp)) {
                    drawFoundStationMarker(shape, center, size.minDimension / 2f)
                }
                Text(shape.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
