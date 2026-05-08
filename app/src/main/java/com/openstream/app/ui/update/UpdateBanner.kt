package com.openstream.app.ui.update

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UpdateBanner(state: AppUpdateUiState, onDismiss: () -> Unit) {
    val visible = state is AppUpdateUiState.UpdateAvailable
    val info    = (state as? AppUpdateUiState.UpdateAvailable)?.info
    val context = LocalContext.current

    AnimatedVisibility(visible = visible, enter = expandVertically(), exit = shrinkVertically()) {
        info ?: return@AnimatedVisibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF7C6FFF).copy(0.25f), Color(0xFF03DAC6).copy(0.15f))),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, Color.White.copy(0.20f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "New version available â€” ${info.versionName}",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (info.changelog.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(info.changelog, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.65f), maxLines = 2)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl)))
                            }
                        },
                        colors    = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier  = Modifier.weight(1f)
                    ) { Text("Update Now") }

                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Later") }
                }
            }
        }
    }
}
