package com.parismetro.quiz.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = MetroBlue,
    secondary = MetroBlueLight,
    background = MetroSurfaceLight,
    surface = MetroSurfaceLight,
    error = IncorrectRed
)

private val DarkColors = darkColorScheme(
    primary = MetroBlueLight,
    secondary = MetroBlue,
    background = MetroSurfaceDark,
    surface = MetroSurfaceDark,
    error = IncorrectRed
)

@Composable
fun MetroQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MetroQuizTypography,
        content = content
    )
}
