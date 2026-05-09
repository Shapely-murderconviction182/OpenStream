package com.openstream.app.ui.screen.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController

@Composable
fun PlayerScreen(
    navController : NavController,
    encodedUrl    : String,
    title         : String        = "",
    viewModel     : PlayerViewModel = hiltViewModel()
) {
    val url      = remember(encodedUrl) {
        runCatching { java.net.URLDecoder.decode(encodedUrl, "UTF-8") }.getOrDefault(encodedUrl)
    }
    val state   by viewModel.state.collectAsState()
    val context  = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    LaunchedEffect(url) { viewModel.prepare(url, title.ifBlank { "Now Playing" }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 2) viewModel.seekRelative(-10_000L)
                        else viewModel.seekRelative(10_000L)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 80f) navController.popBackStack()
                }
            }
    ) {
        // ExoPlayer surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player        = viewModel.player
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error overlay
        state.errorMessage?.let { err ->
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.75f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Playback Error", style = MaterialTheme.typography.titleMedium,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(err, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.65f))
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Icon(imageVector = Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            }
        }

        // Buffering
        if (state.isBuffering && state.errorMessage == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(56.dp), strokeWidth = 3.dp)
            }
        }

        // Controls
        AnimatedVisibility(
            visible = state.showControls && state.errorMessage == null,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f))) {

                // Top bar
                Row(
                    modifier          = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text(state.title, style = MaterialTheme.typography.titleMedium, color = Color.White,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                    SpeedSelector(current = state.playbackSpeed, onSelect = { viewModel.setSpeed(it) })
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        IconButton(onClick = {
                            runCatching {
                                val params = PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9)).build()
                                activity?.enterPictureInPictureMode(params)
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.PictureInPicture, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                // Center controls
                Row(
                    modifier              = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    SeekButton("-10s") { viewModel.seekRelative(-10_000L) }
                    IconButton(
                        onClick  = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp).background(Color.White.copy(0.20f), CircleShape)
                    ) {
                        Icon(
                            imageVector        = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                    SeekButton("+10s") { viewModel.seekRelative(10_000L) }
                }

                // Bottom controls
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    val progress = if (state.duration > 0) state.currentPosition.toFloat() / state.duration.toFloat() else 0f
                    Slider(
                        value         = progress,
                        onValueChange = { pct -> viewModel.player.seekTo((pct * state.duration).toLong()) },
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = SliderDefaults.colors(
                            thumbColor         = Color.White,
                            activeTrackColor   = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(0.35f)
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(formatTime(state.currentPosition), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text(formatTime(state.duration), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.65f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekButton(label: String, onClick: () -> Unit) {
    IconButton(
        onClick  = onClick,
        modifier = Modifier.size(48.dp).background(Color.White.copy(0.12f), CircleShape)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White,
            fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun SpeedSelector(current: Float, onSelect: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    Box {
        TextButton(onClick = { expanded = true }) {
            Text("${current}x", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        DropdownMenu(
            expanded          = expanded,
            onDismissRequest  = { expanded = false },
            modifier          = Modifier.background(Color(0xFF1E1E2A))
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text    = { Text("${speed}x", color = if (speed == current) MaterialTheme.colorScheme.primary else Color.White) },
                    onClick = { onSelect(speed); expanded = false }
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
