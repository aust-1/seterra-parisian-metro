package com.parismetro.quiz.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parismetro.quiz.domain.model.GameConfig
import com.parismetro.quiz.ui.game.GameScreen
import com.parismetro.quiz.ui.game.GameSessionResult
import com.parismetro.quiz.ui.home.HomeScreen
import com.parismetro.quiz.ui.results.ResultsScreen
import com.parismetro.quiz.ui.setup.GameSetupScreen
import com.parismetro.quiz.ui.stats.StatsScreen

private object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val GAME = "game"
    const val RESULTS = "results"
    const val STATS = "stats"
}

/**
 * The whole app is one NavHost. [GameConfig]/[GameSessionResult] are too shaped to serialize
 * cleanly into a nav route string, so they're passed as plain hoisted state instead of nav
 * arguments - simple and sufficient for a single-activity app with no deep linking.
 */
@Composable
fun MetroApp() {
    val navController = rememberNavController()
    var pendingConfig by remember { mutableStateOf<GameConfig?>(null) }
    var lastResult by remember { mutableStateOf<GameSessionResult?>(null) }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = { navController.navigate(Routes.SETUP) },
                onStats = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.SETUP) {
            GameSetupScreen(
                onBack = { navController.popBackStack() },
                onStart = { config ->
                    pendingConfig = config
                    navController.navigate(Routes.GAME)
                }
            )
        }

        composable(Routes.GAME) {
            val config = pendingConfig
            if (config == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.HOME, inclusive = false) }
            } else {
                GameScreen(
                    config = config,
                    onBack = { navController.popBackStack() },
                    onFinished = { result ->
                        lastResult = result
                        navController.navigate(Routes.RESULTS) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.RESULTS) {
            val result = lastResult
            if (result == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.HOME, inclusive = false) }
            } else {
                ResultsScreen(
                    result = result,
                    onReplay = {
                        pendingConfig = result.config
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.RESULTS) { inclusive = true }
                        }
                    },
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
