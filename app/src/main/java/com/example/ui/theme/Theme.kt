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

private val DarkColorScheme = darkColorScheme(
    primary = PolishPrimaryLight,
    onPrimary = PolishPrimaryDark,
    primaryContainer = PolishPrimary,
    onPrimaryContainer = Color.White,
    secondary = SafetyCyanLight,
    onSecondary = SafetyNavy,
    secondaryContainer = SafetyNavyLight,
    onSecondaryContainer = SafetyCyanLight,
    tertiary = WarningAmber,
    onTertiary = SafetyNavy,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedDark,
    onErrorContainer = AlertRedLight,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PolishMutedText,
    outline = Color(0xFF44474E)
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryLight,
    onPrimaryContainer = PolishPrimaryDark,
    secondary = PolishSecondary,
    onSecondary = Color.White,
    secondaryContainer = PolishSecondaryContainer,
    onSecondaryContainer = LightOnSurface,
    tertiary = SafetyCyan,
    onTertiary = Color.White,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedLight,
    onErrorContainer = AlertRedDark,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = PolishMutedText,
    outline = PolishSubtleBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


