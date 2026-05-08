package com.openstream.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.openstream.app.navigation.Screen
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassButton
import com.openstream.app.ui.components.GlassCard

private val PREFS = listOf("Movies", "TV Shows", "Anime", "Live TV", "Documentaries", "Audiobooks")

@Composable
fun OnboardingScreen(
    navController : NavController,
    viewModel     : OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    GlassBackground {
        AnimatedContent(
            targetState = state.currentPage,
            transitionSpec = {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))) togetherWith
                (slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            },
            label = "onboarding_page"
        ) { page ->
            when (page) {
                0 -> WelcomePage    { viewModel.nextPage() }
                1 -> PrefsPage      (state, viewModel)
                2 -> ExtensionsPage (state, viewModel)
                3 -> PermissionsPage{ viewModel.nextPage() }
                4 -> ReadyPage      {
                    viewModel.saveAndFinish {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            }
        }

        // Page dots
        Row(
            modifier              = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (state.currentPage == i) 10.dp else 6.dp)
                        .background(
                            if (state.currentPage == i) Color.White else Color.White.copy(0.35f),
                            CircleShape
                        )
                )
            }
        }
    }
}

// â”€â”€ Page 1: Welcome â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun BoxScope.WelcomePage(onNext: () -> Unit) {
    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF7C6FFF), Color(0xFF03DAC6))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("OS", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(40.dp))
        Text("OpenStream", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Your world of entertainment, open.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.70f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(56.dp))
        GlassButton("Get Started", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}

// â”€â”€ Page 2: Preferences â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun BoxScope.PrefsPage(state: OnboardingState, vm: OnboardingViewModel) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("What do you love?", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Select at least one category", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.60f))
        Spacer(Modifier.height(32.dp))

        PREFS.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { pref ->
                    val selected = pref in state.selectedPrefs
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(0.38f) else Color.White.copy(0.10f))
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(0.22f), RoundedCornerShape(14.dp))
                            .clickable { vm.togglePreference(pref) }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(pref, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(32.dp))
        GlassButton(
            "Continue",
            onClick  = { if (state.selectedPrefs.isNotEmpty()) vm.nextPage() },
            modifier = Modifier.fillMaxWidth(),
            enabled  = state.selectedPrefs.isNotEmpty()
        )
    }
}

// â”€â”€ Page 3: Extensions Setup â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun BoxScope.ExtensionsPage(state: OnboardingState, vm: OnboardingViewModel) {
    LaunchedEffect(Unit) { vm.loadExtensions() }

    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        if (!state.extensionLoadDone) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(32.dp))
        } else {
            Box(
                modifier = Modifier.size(64.dp).background(Color(0xFF4CAF50).copy(0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
        Text(state.extensionLoadMsg, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Extensions power your streaming sources", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.60f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        GlassButton(if (state.extensionLoadDone) "Continue" else "Skip", onClick = { vm.nextPage() }, modifier = Modifier.fillMaxWidth())
    }
}

// â”€â”€ Page 4: Permissions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun BoxScope.PermissionsPage(onNext: () -> Unit) {
    val storageGranted = remember { mutableStateOf(false) }
    val notifGranted   = remember { mutableStateOf(false) }

    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        storageGranted.value = it
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifGranted.value = it
    }

    Column(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permissions", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Optional â€” you can change these later", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.60f))
        Spacer(Modifier.height(32.dp))

        PermissionCard(
            icon    = Icons.Default.Lock,
            title   = "Storage",
            desc    = "Needed to download content for offline viewing",
            granted = storageGranted.value,
            onAllow = {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                else storageGranted.value = true
            }
        )
        Spacer(Modifier.height(16.dp))
        PermissionCard(
            icon    = Icons.Default.Notifications,
            title   = "Notifications",
            desc    = "Get notified when extensions update",
            granted = notifGranted.value,
            onAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else notifGranted.value = true
            }
        )

        Spacer(Modifier.height(40.dp))
        GlassButton("Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now", color = Color.White.copy(0.50f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PermissionCard(
    icon    : ImageVector,
    title   : String,
    desc    : String,
    granted : Boolean,
    onAllow : () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(desc,  style = MaterialTheme.typography.bodySmall,  color = Color.White.copy(0.60f))
            }
            Spacer(Modifier.width(12.dp))
            if (granted) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = onAllow, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Allow", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// â”€â”€ Page 5: Ready â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun BoxScope.ReadyPage(onStart: () -> Unit) {
    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF7C6FFF), Color(0xFF03DAC6))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(40.dp))
        Text("OpenStream is ready", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Everything is set up.\nEnjoy your entertainment.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.70f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(56.dp))
        GlassButton("Start Watching", onClick = onStart, modifier = Modifier.fillMaxWidth())
    }
}
