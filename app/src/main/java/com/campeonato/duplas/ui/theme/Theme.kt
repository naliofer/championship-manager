package com.campeonato.duplas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppColors = darkColorScheme(
    primary = AppPrimary,
    onPrimary = AppBackground,
    primaryContainer = AppSecondary,
    onPrimaryContainer = AppText,
    secondary = AppMutedText,
    onSecondary = AppBackground,
    secondaryContainer = AppSecondary,
    onSecondaryContainer = AppText,
    tertiary = AppWin,
    onTertiary = AppBackground,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    surfaceContainer = AppSurface,
    surfaceContainerLowest = AppBackground,
    surfaceContainerLow = AppSurface,
    surfaceContainerHigh = AppSurfaceSecondary,
    surfaceContainerHighest = AppSecondary,
    onSurface = AppText,
    error = AppLoss,
    onError = AppText,
    outline = AppDivider,
    outlineVariant = AppSecondary,
    surfaceVariant = AppSurfaceSecondary,
    onSurfaceVariant = AppMutedText
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun CampeonatoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
