package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.repository.DarkModeTheme

private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandGreenPrimary,
    onPrimary = DarkBrandGreenOnPrimary,
    primaryContainer = DarkBrandGreenContainer,
    onPrimaryContainer = DarkBrandGreenOnContainer,
    secondary = DarkAccentGreen,
    onSecondary = DarkBrandGreenOnPrimary,
    secondaryContainer = DarkAccentGreenContainer,
    onSecondaryContainer = DarkBrandGreenOnContainer,
    tertiary = DarkAccentGreen,
    background = NeutralDarkBackground,
    onBackground = TextPrimaryDark,
    surface = NeutralDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NeutralDarkBorder,
    outlineVariant = NeutralDarkBorder,
    error = ErrorDark,
    onError = DarkBrandGreenOnPrimary,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGreenPrimary,
    onPrimary = BrandGreenOnPrimary,
    primaryContainer = BrandGreenContainer,
    onPrimaryContainer = BrandGreenOnContainer,
    secondary = AccentGreen,
    onSecondary = BrandGreenOnPrimary,
    secondaryContainer = AccentGreenContainer,
    onSecondaryContainer = BrandGreenOnContainer,
    tertiary = AccentGreen,
    background = NeutralLightBackground,
    onBackground = TextPrimaryLight,
    surface = NeutralLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = NeutralLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = NeutralLightBorder,
    outlineVariant = NeutralLightBorder,
    error = ErrorLight,
    onError = BrandGreenOnPrimary,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorLight
)

@Composable
fun HaghEManTheme(
    darkModeTheme: DarkModeTheme = DarkModeTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkModeTheme) {
        DarkModeTheme.SYSTEM -> isSystemInDarkTheme()
        DarkModeTheme.LIGHT -> false
        DarkModeTheme.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
