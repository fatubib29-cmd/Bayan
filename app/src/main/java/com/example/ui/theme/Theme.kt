package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = SurfaceWhite,
    primaryContainer = DeepTealContainer,
    onPrimaryContainer = DeepTealDark,
    secondary = AntiqueGold,
    onSecondary = SurfaceWhite,
    secondaryContainer = AntiqueGoldContainer,
    onSecondaryContainer = AntiqueGoldDark,
    tertiary = MutedBlueGrey,
    onTertiary = TextDarkPrimary,
    background = WarmIvory,
    onBackground = TextDarkPrimary,
    surface = WarmIvorySurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = WarmIvoryContainer,
    onSurfaceVariant = TextDarkSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderGlass
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DeepTeal,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkGoldSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = AntiqueGoldDark,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = MutedBlueGrey,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = AntiqueGold
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand identity by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
