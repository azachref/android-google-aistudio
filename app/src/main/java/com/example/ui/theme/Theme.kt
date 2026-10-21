package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SomniaColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = AmoledBlack,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = Color.White,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = NeonVioletDark,
    onSecondaryContainer = Color.White,
    tertiary = NeonMint,
    onTertiary = Color.White,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = MidnightDark,
    onSurface = TextPrimary,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderActive
)

@Composable
fun SomniaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SomniaColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SomniaTheme(content = content)
}
