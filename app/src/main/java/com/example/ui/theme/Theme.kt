package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = OnNeonLime,
    primaryContainer = NeonLimeContainer,
    onPrimaryContainer = NeonLime,

    secondary = CyanAccent,
    onSecondary = OnCyanAccent,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanAccent,

    tertiary = CyanAccentVariant,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceCardDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSecondaryDark,
    onSurfaceVariant = TextSecondary,

    outline = SurfaceBorderDark,
    outlineVariant = SurfaceBorderDark.copy(alpha = 0.5f),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),

    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF006064),

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSecondaryLight,
    onSurfaceVariant = TextSecondaryLight,

    outline = SurfaceBorderLight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun LifeOSTheme(
    darkTheme: Boolean = true, // Premium Dark by default as required
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
