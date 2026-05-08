package com.openstream.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openstream.app.ui.onboarding.OnboardingScreen
import com.openstream.app.ui.screen.HomeScreen
import com.openstream.app.ui.screen.extensions.ExtensionsScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home       : Screen("home")
    object Extensions : Screen("extensions")
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
    }
}
