package com.openstream.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick        = onClick,
        modifier       = modifier,
        enabled        = enabled,
        shape          = RoundedCornerShape(14.dp),
        colors         = ButtonDefaults.buttonColors(
            containerColor         = Color.White.copy(alpha = 0.15f),
            contentColor           = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.06f),
            disabledContentColor   = Color.White.copy(alpha = 0.40f)
        ),
        border         = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.50f), Color.White.copy(0.20f)))),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text) }
}
