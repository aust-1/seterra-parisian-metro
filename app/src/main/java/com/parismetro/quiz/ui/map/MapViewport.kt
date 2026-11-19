package com.parismetro.quiz.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlin.math.min

/** Bounds of [MapViewport.scale], in screen pixels per map unit. */
const val MIN_MAP_SCALE = 0.6f
const val MAX_MAP_SCALE = 6f

/**
 * What part of the map is on screen: a uniform [scale] (screen pixels per map unit) and the
 * map-space point shown at the centre of the view ([centerX], [centerY]).
 *
 * Anchoring on the centre rather than on a top-left offset is what lets the view survive a
 * size change: after a rotation the same spot stays in the middle at the same zoom, instead of
 * drifting towards a corner or snapping back to the full-map framing. Pure Kotlin, unit-tested.
 *
 * [isFit] marks the automatic "whole map" framing: as long as the player hasn't zoomed or
 * panned, a size change re-fits the map to the new size instead of keeping the old scale.
 */
data class MapViewport(
    val scale: Float,
    val centerX: Float,
    val centerY: Float,
    val isFit: Boolean = false
) {

    /** Screen position of map point (0, 0) in a view of [viewWidth] x [viewHeight] pixels. */
    fun offsetX(viewWidth: Float): Float = viewWidth / 2f - centerX * scale
    fun offsetY(viewHeight: Float): Float = viewHeight / 2f - centerY * scale

    /** The viewport to use once the view is [viewWidth] x [viewHeight]: re-fitted if it was
     * the automatic framing, otherwise the same zoom and centre (see the class doc). */
    fun resized(viewWidth: Float, viewHeight: Float): MapViewport =
        if (isFit) fitting(viewWidth, viewHeight) else this

    /**
     * Applies one pinch/drag step: zooms by [zoom] around the gesture's centroid (the map point
     * under the fingers stays put), then pans by ([panX], [panY]) screen pixels.
     */
    fun transformed(
        centroidX: Float,
        centroidY: Float,
        panX: Float,
        panY: Float,
        zoom: Float,
        viewWidth: Float,
        viewHeight: Float
    ): MapViewport {
        val newScale = (scale * zoom).coerceIn(MIN_MAP_SCALE, MAX_MAP_SCALE)
        val mapCentroidX = (centroidX - offsetX(viewWidth)) / scale
        val mapCentroidY = (centroidY - offsetY(viewHeight)) / scale
        val newOffsetX = centroidX - mapCentroidX * newScale + panX
        val newOffsetY = centroidY - mapCentroidY * newScale + panY
        return MapViewport(
            scale = newScale,
            centerX = (viewWidth / 2f - newOffsetX) / newScale,
            centerY = (viewHeight / 2f - newOffsetY) / newScale
        )
    }

    companion object {
        /** The whole map, centred and as large as fits in the view. */
        fun fitting(
            viewWidth: Float,
            viewHeight: Float,
            mapWidth: Float = MAP_WIDTH,
            mapHeight: Float = MAP_HEIGHT
        ) = MapViewport(
            scale = min(viewWidth / mapWidth, viewHeight / mapHeight),
            centerX = mapWidth / 2f,
            centerY = mapHeight / 2f,
            isFit = true
        )
    }
}

/**
 * Holder for the map's [viewport], kept across configuration changes (rotation, theme switch)
 * and process death with `rememberSaveable(saver = MapViewportState.Saver)`. [viewport] is null
 * until the first layout, when the canvas fits the whole map in.
 */
class MapViewportState(initial: MapViewport? = null) {
    var viewport: MapViewport? by mutableStateOf(initial)

    companion object {
        val Saver: Saver<MapViewportState, FloatArray> = Saver(
            save = { state ->
                state.viewport?.let { floatArrayOf(it.scale, it.centerX, it.centerY, if (it.isFit) 1f else 0f) }
            },
            restore = { saved -> MapViewportState(MapViewport(saved[0], saved[1], saved[2], isFit = saved[3] == 1f)) }
        )
    }
}
