package com.openstream.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.openstream.app.navigation.Screen
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassButton
import com.openstream.app.ui.components.GlassCard
import com.openstream.app.ui.components.GlassTopBar
import com.openstream.app.ui.theme.ThemeMode
import com.openstream.app.ui.theme.ThemeViewModel
import com.openstream.app.ui.update.AppUpdateViewModel
import com.openstream.app.ui.update.UpdateBanner

@Composable
fun HomeScreen(
    navController  : NavController,
    themeViewModel : ThemeViewModel    = hiltViewModel(),
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val themeMode   by themeViewModel.themeMode.collectAsState()
    val updateState by updateViewModel.state.collectAsState()

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(title = {
                    Text("OpenStream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                })
            }
        ) { pad ->
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                // Update banner
                item {
                    UpdateBanner(state = updateState, onDismiss = { updateViewModel.dismiss() })
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Welcome to OpenStream", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Your glass-themed streaming experience.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.72f))
                            Spacer(Modifier.height(16.dp))
                            GlassButton("Get Started", onClick = {})
                        }
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Theme", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeMode.values().forEach { mode ->
                                    GlassButton(mode.name, onClick = { themeViewModel.setTheme(mode) }, modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Active: ${themeMode.name}", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.55f))
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureCard(Modifier.weight(1f), "Library",    "Browse your content", onClick = {})
                        FeatureCard(Modifier.weight(1f), "Extensions", "Add new sources",     onClick = { navController.navigate(Screen.Extensions.route) })
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun FeatureCard(modifier: Modifier = Modifier, title: String, body: String, onClick: () -> Unit) {
    GlassCard(modifier = modifier.clickable { onClick() }, cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White.copy(0.80f))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(body,  style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.60f))
        }
    }
}
