package com.parismetro.quiz.ui.map

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.parismetro.quiz.domain.model.MapPoint
import com.parismetro.quiz.domain.model.MetroLine
import com.parismetro.quiz.domain.model.MissMarker
import com.parismetro.quiz.domain.model.Station
import com.parismetro.quiz.ui.theme.CorrectGreen
import com.parismetro.quiz.ui.theme.HighlightAmber
import com.parismetro.quiz.ui.theme.IncorrectRed
import kotlin.math.min

/**
 * Renders the schematic metro map and owns its own pan/zoom state.
 *
 * The map-space -> screen-space transform (a uniform [scale] plus an [offset], no rotation) is
 * applied by hand in [toScreen] rather than via `Modifier.graphicsLayer`, specifically so that
 * raw pointer coordinates from [pointerInput] stay in plain screen space and the exact same
 * [offset]/[scale] can be inverted for hit-testing - one transform, defined once, used both ways.
 *
 * @param interactive when true (map-click mode), taps resolve to [onStationTapped] /
 *   [onMapMissed]; when false (QCM / type-answer modes) the map is pan/zoom-only, used just to
 *   show [highlightedStationId] in context.
 */
@Composable
fun MetroMapCanvas(
    stations: List<Station>,
    lines: List<MetroLine>,
    modifier: Modifier = Modifier,
    showLines: Boolean = true,
    highlightedStationId: String? = null,
    correctStationId: String? = null,
    missMarkers: List<MissMarker> = emptyList(),
    interactive: Boolean = false,
    onStationTapped: (Station) -> Unit = {},
    onMapMissed: (MapPoint) -> Unit = {}
) {
    val stationsById = remember(stations) { stations.associateBy { it.id } }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var fitted by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "highlight-pulse")
    val pulseRadius by pulse.animateFloat(
        initialValue = 14f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse-radius"
    )

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                if (!fitted && size.width > 0 && size.height > 0) {
                    val fitScale = min(size.width / MAP_WIDTH, size.height / MAP_HEIGHT)
                    scale = fitScale
                    offset = Offset(
                        (size.width - MAP_WIDTH * fitScale) / 2f,
                        (size.height - MAP_HEIGHT * fitScale) / 2f
                    )
                    fitted = true
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.6f, 6f)
                    // Keep the map point under the gesture's centroid fixed while scaling, then
                    // apply the pan on top - see the class doc for why this math lives here.
                    val mapCentroid = (centroid - offset) / scale
                    offset = centroid - mapCentroid * newScale + pan
                    scale = newScale
                }
            }
            .pointerInput(interactive) {
                if (!interactive) return@pointerInput
                detectTapGestures { tapOffset ->
                    val mapPoint = (tapOffset - offset) / scale
                    val nearest = stations.minByOrNull { station ->
                        squaredDistance(station.x, station.y, mapPoint.x, mapPoint.y)
                    }
                    val hit = nearest?.takeIf { station ->
                        squaredDistance(station.x, station.y, mapPoint.x, mapPoint.y) <=
                            TAP_HIT_RADIUS * TAP_HIT_RADIUS
                    }
                    if (hit != null) {
                        onStationTapped(hit)
                    } else {
                        onMapMissed(MapPoint(mapPoint.x, mapPoint.y))
                    }
                }
            }
    ) {
        fun toScreen(x: Float, y: Float) = Offset(offset.x + x * scale, offset.y + y * scale)
        val dotScaleFactor = scale.coerceIn(0.6f, 2.5f)

        if (showLines) {
            lines.forEach { line ->
                val color = parseHexColor(line.color)
                line.paths.forEach { path ->
                    for (i in 0 until path.size - 1) {
                        val from = stationsById[path[i]] ?: continue
                        val to = stationsById[path[i + 1]] ?: continue
                        drawLine(
                            color = color,
                            start = toScreen(from.x, from.y),
                            end = toScreen(to.x, to.y),
                            strokeWidth = (5f * scale).coerceIn(2f, 10f)
                        )
                    }
                }
            }
        }

        stations.forEach { station ->
            val center = toScreen(station.x, station.y)
            val isInterchange = station.lineIds.size >= 2
            val radius = (if (isInterchange) 6f else 4f) * dotScaleFactor
            drawCircle(color = Color.White, radius = radius, center = center)
            drawCircle(color = Color.DarkGray, radius = radius, center = center, style = Stroke(width = 1.5f))
        }

        missMarkers.forEach { marker ->
            val center = toScreen(marker.x, marker.y)
            val arm = 8f
            drawLine(IncorrectRed, center + Offset(-arm, -arm), center + Offset(arm, arm), strokeWidth = 4f)
            drawLine(IncorrectRed, center + Offset(-arm, arm), center + Offset(arm, -arm), strokeWidth = 4f)
        }

        highlightedStationId?.let { id ->
            stationsById[id]?.let { station ->
                val center = toScreen(station.x, station.y)
                drawCircle(color = HighlightAmber.copy(alpha = 0.35f), radius = pulseRadius * dotScaleFactor, center = center)
                drawCircle(color = HighlightAmber, radius = 8f * dotScaleFactor, center = center)
            }
        }

        correctStationId?.let { id ->
            stationsById[id]?.let { station ->
                drawCircle(color = CorrectGreen, radius = 9f * dotScaleFactor, center = toScreen(station.x, station.y))
            }
        }
    }
}

private fun squaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return dx * dx + dy * dy
}
