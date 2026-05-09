package com.openstream.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openstream.app.ui.onboarding.OnboardingScreen
import com.openstream.app.ui.screen.HomeScreen
import com.openstream.app.ui.screen.detail.DetailScreen
import com.openstream.app.ui.screen.extensions.ExtensionsScreen
import com.openstream.app.ui.screen.player.PlayerScreen
import com.openstream.app.ui.screen.search.SearchScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home       : Screen("home")
    object Extensions : Screen("extensions")
    object Search     : Screen("search")
    object Detail     : Screen("detail/{encodedUrl}") {
        fun createRoute(encodedUrl: String) = "detail/$encodedUrl"
    }
    object Player     : Screen("player/{encodedUrl}") {
        fun createRoute(encodedUrl: String) = "player/$encodedUrl"
    }
}

@Composable
fun AppNavGraph(
    startDestination : String,
    navController    : NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Extensions.route) {
            ExtensionsScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(
            route     = Screen.Detail.route,
            arguments = listOf(navArgument("encodedUrl") { type = NavType.StringType })
        ) { back ->
            val encodedUrl = back.arguments?.getString("encodedUrl") ?: ""
            DetailScreen(navController = navController, encodedUrl = encodedUrl)
        }
        composable(
            route     = Screen.Player.route,
            arguments = listOf(navArgument("encodedUrl") { type = NavType.StringType })
        ) { back ->
            val encodedUrl = back.arguments?.getString("encodedUrl") ?: ""
            PlayerScreen(navController = navController, encodedUrl = encodedUrl)
        }
    }
}
