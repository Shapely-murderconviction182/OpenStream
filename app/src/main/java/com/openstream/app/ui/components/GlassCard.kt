package com.openstream.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier       : Modifier  = Modifier,
    cornerRadius   : Dp        = 20.dp,
    contentPadding : Dp        = 16.dp,
    alpha          : Float     = 0.15f,
    content        : @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val blurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = BlurEffect(10f, 10f, TileMode.Clamp)
        }
    } else Modifier

    Box(
        modifier = modifier
            .then(blurMod)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha + 0.05f), Color.White.copy(alpha * 0.4f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(0.55f), Color.White.copy(0.15f), Color.White.copy(0.35f))
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}
