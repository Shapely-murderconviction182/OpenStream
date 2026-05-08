package com.openstream.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.openstream.app.data.OnboardingRepository
import com.openstream.app.navigation.AppNavGraph
import com.openstream.app.navigation.Screen
import com.openstream.app.ui.theme.AppTheme
import com.openstream.app.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingRepo: OnboardingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Read onboarding state synchronously (one-time, fast DataStore read)
        val onboardingDone = runBlocking { onboardingRepo.isOnboardingComplete.first() }
        val start = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            AppTheme(themeMode = themeMode) {
                AppNavGraph(startDestination = start)
            }
        }
    }
}
