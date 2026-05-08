package com.openstream.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val schemeDark = darkColorScheme(
    primary          = DarkPrimary,
    secondary        = DarkSecondary,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkSurfaceVar,
    onPrimary        = DarkOnPrimary,
    onBackground     = DarkOnBackground,
    onSurface        = DarkOnSurface,
    onSurfaceVariant = DarkOnSurface
)

private val schemeAmoled = darkColorScheme(
    primary          = DarkPrimary,
    secondary        = DarkSecondary,
    background       = AmoledBackground,
    surface          = AmoledSurface,
    surfaceVariant   = AmoledSurfaceVar,
    onPrimary        = DarkOnPrimary,
    onBackground     = DarkOnBackground,
    onSurface        = DarkOnSurface,
    onSurfaceVariant = DarkOnSurface
)

private val schemeLight = lightColorScheme(
    primary          = LightPrimary,
    secondary        = DarkSecondary,
    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = LightSurfaceVar,
    onPrimary        = Color.White,
    onBackground     = LightOnBackground,
    onSurface        = LightOnSurface,
    onSurfaceVariant = LightOnSurface
)

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.DARK }

@Composable
fun AppTheme(themeMode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.DARK   -> schemeDark
        ThemeMode.AMOLED -> schemeAmoled
        ThemeMode.LIGHT  -> schemeLight
        ThemeMode.SYSTEM -> if (systemDark) schemeDark else schemeLight
    }
    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
    }
}
