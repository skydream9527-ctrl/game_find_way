package com.gameway.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gameway.domain.model.CharacterType
import org.koin.androidx.compose.koinViewModel
import com.gameway.presentation.screens.character.CharacterSelectScreen
import com.gameway.presentation.screens.splash.SplashScreen
import com.gameway.presentation.screens.menu.MainMenuScreen
import com.gameway.presentation.screens.chapter.ChapterSelectScreen
import com.gameway.presentation.screens.level.LevelSelectScreen
import com.gameway.presentation.screens.game.GameScreen
import com.gameway.presentation.screens.leaderboard.LeaderboardScreen
import com.gameway.presentation.screens.leaderboard.LeaderboardViewModel
import com.gameway.presentation.screens.stats.StatsScreen
import com.gameway.presentation.screens.friend.FriendScreen
import com.gameway.presentation.screens.friend.FriendViewModel
import com.gameway.presentation.screens.match.MatchScreen
import com.gameway.presentation.screens.match.MatchViewModel
import com.gameway.presentation.screens.skinstore.SkinStoreScreen
import com.gameway.presentation.screens.skinstore.SkinStoreViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object MainMenu : Screen("main_menu")
    data object CharacterSelect : Screen("character_select")
    data object ChapterSelect : Screen("chapter_select")
    data class LevelSelect(val chapterId: Int) : Screen("level_select/$chapterId") {
        companion object {
            const val ROUTE_PATTERN = "level_select/{chapterId}"
        }
    }
    data class Game(val chapterId: Int, val levelNumber: Int, val characterType: CharacterType) : Screen("game/$chapterId/$levelNumber/$characterType") {
        companion object {
            const val ROUTE_PATTERN = "game/{chapterId}/{levelNumber}/{characterType}"
        }
    }
    data object Stats : Screen("stats")
    data object Leaderboard : Screen("leaderboard")
    data object Friend : Screen("friend")
    data object Match : Screen("match")
    data object SkinStore : Screen("skin_store")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    var selectedCharacterType by remember { mutableStateOf(CharacterType.CAT) }
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToMenu = {
                navController.navigate(Screen.MainMenu.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
        }
        
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onStartGame = { navController.navigate(Screen.ChapterSelect.route) },
                onSelectCharacter = { navController.navigate(Screen.CharacterSelect.route) },
                onViewStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onNavigateToFriend = { navController.navigate(Screen.Friend.route) },
                onNavigateToMatch = { navController.navigate(Screen.Match.route) },
                onNavigateToSkinStore = { navController.navigate(Screen.SkinStore.route) }
            )
        }
        
        composable(Screen.CharacterSelect.route) {
            CharacterSelectScreen(
                currentCharacter = selectedCharacterType,
                onCharacterSelected = { type ->
                    selectedCharacterType = type
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
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
                onLevelSelected = { levelNumber -> 
                    navController.navigate(Screen.Game(chapterId, levelNumber, selectedCharacterType).route) 
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Game.ROUTE_PATTERN) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toIntOrNull() ?: 1
            val levelNumber = backStackEntry.arguments?.getString("levelNumber")?.toIntOrNull() ?: 1
            val characterTypeString = backStackEntry.arguments?.getString("characterType") ?: "CAT"
            val characterType = CharacterType.valueOf(characterTypeString)
            GameScreen(
                chapterId = chapterId,
                levelNumber = levelNumber,
                characterType = characterType,
                onLevelComplete = { navController.popBackStack() },
                onLevelFailed = { navController.popBackStack() },
                onBackToMenu = { navController.popBackStack(Screen.MainMenu.route, inclusive = false) }
            )
        }
        
        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                viewModel = koinViewModel<LeaderboardViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Friend.route) {
            FriendScreen(
                viewModel = koinViewModel<FriendViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Match.route) {
            MatchScreen(
                viewModel = koinViewModel<MatchViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SkinStore.route) {
            SkinStoreScreen(
                viewModel = koinViewModel<SkinStoreViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}