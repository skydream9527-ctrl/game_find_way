package com.gameway.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gameway.presentation.screens.splash.SplashScreen
import com.gameway.presentation.screens.menu.MainMenuScreen
import com.gameway.presentation.screens.chapter.ChapterSelectScreen
import com.gameway.presentation.screens.level.LevelSelectScreen
import com.gameway.presentation.screens.game.GameScreen
import com.gameway.presentation.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object MainMenu : Screen("main_menu")
    data object ChapterSelect : Screen("chapter_select")
    data class LevelSelect(val chapterId: Int) : Screen("level_select/$chapterId") {
        companion object {
            const val ROUTE_PATTERN = "level_select/{chapterId}"
        }
    }
    data class Game(val chapterId: Int, val levelNumber: Int) : Screen("game/$chapterId/$levelNumber") {
        companion object {
            const val ROUTE_PATTERN = "game/{chapterId}/{levelNumber}"
        }
    }
    data object Stats : Screen("stats")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToMenu = {
                navController.navigate(Screen.MainMenu.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
        }
        
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onStartGame = { navController.navigate(Screen.ChapterSelect.route) },
                onViewStats = { navController.navigate(Screen.Stats.route) }
            )
        }
        
        composable(Screen.ChapterSelect.route) {
            ChapterSelectScreen(
                onChapterSelected = { chapterId -> navController.navigate(Screen.LevelSelect(chapterId).route) },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.LevelSelect.ROUTE_PATTERN) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toIntOrNull() ?: 1
            LevelSelectScreen(
                chapterId = chapterId,
                onLevelSelected = { levelNumber -> navController.navigate(Screen.Game(chapterId, levelNumber).route) },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Game.ROUTE_PATTERN) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toIntOrNull() ?: 1
            val levelNumber = backStackEntry.arguments?.getString("levelNumber")?.toIntOrNull() ?: 1
            GameScreen(
                chapterId = chapterId,
                levelNumber = levelNumber,
                onLevelComplete = { navController.popBackStack() },
                onLevelFailed = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}