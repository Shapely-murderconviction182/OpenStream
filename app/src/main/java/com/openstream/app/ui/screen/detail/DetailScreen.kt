package com.openstream.app.ui.screen.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.openstream.app.core.plugins.LoadResponse
import com.openstream.app.navigation.Screen
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassCard
import com.openstream.app.ui.components.GlassChip
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun DetailScreen(
    navController : NavController,
    encodedUrl    : String,
    viewModel     : DetailViewModel = hiltViewModel()
) {
    val url     = remember(encodedUrl) {
        runCatching { java.net.URLDecoder.decode(encodedUrl, "UTF-8") }.getOrDefault(encodedUrl)
    }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(url) { viewModel.load(url) }

    GlassBackground {
        when (val state = uiState) {
            is DetailUiState.Loading -> DetailLoadingView()
            is DetailUiState.Error   -> DetailErrorView(state.message) { viewModel.load(url) }
            is DetailUiState.Success -> DetailContentView(data = state.data, navController = navController)
        }

        // Floating back button
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Black.copy(0.55f), CircleShape)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun BoxScope.DetailContentView(data: LoadResponse, navController: NavController) {
    var synopsisExpanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Backdrop
        item {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                AsyncImage(
                    model              = data.posterUrl.ifBlank { null },
                    contentDescription = data.name,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(0.20f), Color.Black.copy(0.92f)))
                    )
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(data.name, style = MaterialTheme.typography.headlineMedium, color = Color.White,
                        fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (data.sourceName.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(data.sourceName, style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.60f), fontStyle = FontStyle.Italic)
                    }
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick  = {
                        val encoded = URLEncoder.encode(data.url, StandardCharsets.UTF_8.toString())
                        navController.navigate(Screen.Player.createRoute(encoded))
                    },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Play Now", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick        = {},
                    shape          = RoundedCornerShape(12.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier       = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                OutlinedButton(
                    onClick        = {},
                    shape          = RoundedCornerShape(12.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier       = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Metadata
        item {
            Row(
                modifier              = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                GlassChip(data.url.take(4).uppercase())
            }
            Spacer(Modifier.height(12.dp))
        }

        // Synopsis
        if (data.description.isNotBlank()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column {
                        Text("Synopsis", style = MaterialTheme.typography.titleSmall,
                            color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = data.description,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Color.White.copy(0.80f),
                            maxLines = if (synopsisExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text     = if (synopsisExpanded) "Show less" else "Read more",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { synopsisExpanded = !synopsisExpanded }
                        )
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailLoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DetailErrorView(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Could not load details", style = MaterialTheme.typography.titleMedium,
                    color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(msg, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.55f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Retry")
                }
            }
        }
    }
}
