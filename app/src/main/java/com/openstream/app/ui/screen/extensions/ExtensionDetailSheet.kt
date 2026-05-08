package com.openstream.app.ui.screen.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openstream.app.data.local.ExtensionEntity
import com.openstream.app.ui.components.GlassButton
import com.openstream.app.ui.components.GlassChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionDetailSheet(
    extension : ExtensionEntity,
    onDismiss : () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF13131E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.28f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // â”€â”€ Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExtensionIconView(iconUrl = extension.iconUrl, name = extension.name, size = 56)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = extension.name,
                        style      = MaterialTheme.typography.titleLarge,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "v${extension.version}  â€¢  ${extension.repoName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.50f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (extension.isInstalled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(20.dp))

            // â”€â”€ Description â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (extension.description.isNotBlank()) {
                Text(
                    text  = extension.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(16.dp))
            }

            // â”€â”€ Chips â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(extension.language)
                GlassChip(extension.type)
                if (extension.isInstalled) {
                    GlassChip(text = "Installed", textColor = Color(0xFF4CAF50))
                }
            }

            Spacer(Modifier.height(24.dp))

            // â”€â”€ Action button (UI only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            GlassButton(
                text     = if (extension.isInstalled) "Remove Extension" else "Install Extension",
                onClick  = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
