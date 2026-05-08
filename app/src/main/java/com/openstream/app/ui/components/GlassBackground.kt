package com.openstream.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GlassBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val bg      = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .drawBehind {
                drawCircle(
                    brush  = Brush.radialGradient(listOf(primary.copy(0.20f), Color.Transparent), Offset(size.width * 0.10f, size.height * 0.08f), size.width * 0.65f),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.10f, size.height * 0.08f)
                )
                drawCircle(
                    brush  = Brush.radialGradient(listOf(primary.copy(0.13f), Color.Transparent), Offset(size.width * 0.90f, size.height * 0.88f), size.width * 0.55f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.90f, size.height * 0.88f)
                )
                drawCircle(
                    brush  = Brush.radialGradient(listOf(primary.copy(0.06f), Color.Transparent), Offset(size.width * 0.50f, size.height * 0.45f), size.width * 0.40f),
                    radius = size.width * 0.40f,
                    center = Offset(size.width * 0.50f, size.height * 0.45f)
                )
            },
        content = content
    )
}
