package com.parismetro.quiz.ui.map

/**
 * The coordinate space every station's x/y and every line's paths are expressed in.
 * Matches the `svgViewBox` recorded in `scripts/metro-source.json` (width 890 / height 1120) -
 * the bundled `stations.json` was generated against it. The projection has the same scale on
 * both axes (about 40 units per km), so the frame is portrait: the network is taller than wide.
 */
const val MAP_WIDTH = 890f
const val MAP_HEIGHT = 1120f

/** How close (in map-space units) a tap needs to be to a station to count as hitting it. */
const val TAP_HIT_RADIUS = 26f
