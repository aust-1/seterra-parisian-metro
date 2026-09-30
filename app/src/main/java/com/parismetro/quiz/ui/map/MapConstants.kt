package com.parismetro.quiz.ui.map

/**
 * The coordinate space every station's x/y and every line's paths are expressed in.
 * Matches the `svgViewBox` recorded in the previous project's `metro-source.json`
 * (width 1400 / height 1120) - the bundled `stations.json` was generated against it.
 */
const val MAP_WIDTH = 1400f
const val MAP_HEIGHT = 1120f

/** How close (in map-space units) a tap needs to be to a station to count as hitting it. */
const val TAP_HIT_RADIUS = 26f
