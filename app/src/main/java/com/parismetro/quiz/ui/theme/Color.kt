package com.parismetro.quiz.ui.theme

import androidx.compose.ui.graphics.Color

// A calm, transit-map-inspired palette - deep blue primary (echoes RATP signage), a single
// warm accent for "correct", a single red for "wrong"/miss markers. Kept deliberately small:
// individual line colors (from lines.json) carry the visual variety on the map itself.
val MetroBlue = Color(0xFF0D3B66)
val MetroBlueLight = Color(0xFF3A6EA5)
val MetroSurfaceLight = Color(0xFFF7F9FC)
val MetroSurfaceDark = Color(0xFF10151C)

val CorrectGreen = Color(0xFF2E7D32)
val IncorrectRed = Color(0xFFC62828)
val HighlightAmber = Color(0xFFFFB300)
val MissOrange = Color(0xFFEF6C00)
val FoundYellow = Color(0xFFFBC02D)

/** Per-attempt-count colors for a found station: index = miss count (0..2), last = revealed. */
val FOUND_STATION_COLORS = listOf(CorrectGreen, FoundYellow, MissOrange, IncorrectRed)

// Geographic context on the map itself - deliberately muted so the colorful metro lines (drawn
// on top) stay the clear focal point; these just help the map read as "Paris" at a glance.
val SeineBlue = Color(0xFFA9D3E8)
val PeripheriqueGray = Color(0xFFAFAFAF)
