package com.parismetro.quiz.ui.map

import androidx.compose.ui.graphics.Color

/** Parses a "#RRGGBB" hex string (as shipped in lines.json) into a Compose [Color]. */
fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}
