package com.controlprestamos.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ControlPrestamosLightScheme = lightColorScheme(
    primary = AppColors.PrimaryDark,
    onPrimary = AppColors.Surface,
    secondary = AppColors.AccentTeal,
    onSecondary = AppColors.Surface,
    tertiary = AppColors.PrimaryNavy,
    onTertiary = AppColors.Surface,
    background = AppColors.Background,
    onBackground = AppColors.Gray900,
    surface = AppColors.Surface,
    onSurface = AppColors.Gray900,
    surfaceVariant = AppColors.Gray100,
    onSurfaceVariant = AppColors.Gray600,
    outline = AppColors.Gray300,
    error = AppColors.Error,
    onError = AppColors.Surface
)

@Composable
fun ControlPrestamosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ControlPrestamosLightScheme,
        typography = AppTypography,
        content = content
    )
}

