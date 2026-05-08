package com.openstream.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title          : @Composable () -> Unit,
    modifier       : Modifier = Modifier,
    navigationIcon : @Composable () -> Unit = {},
    actions        : @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.20f), Color.Transparent)
                ),
                shape = RectangleShape
            )
    ) {
        TopAppBar(
            title          = title,
            modifier       = modifier,
            navigationIcon = navigationIcon,
            actions        = actions,
            colors         = TopAppBarDefaults.topAppBarColors(
                containerColor             = Color.Transparent,
                titleContentColor          = Color.White,
                actionIconContentColor     = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }
}
