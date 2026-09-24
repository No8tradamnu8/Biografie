package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkDiaryColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = LeatherDark,
    primaryContainer = LeatherWarm,
    onPrimaryContainer = GoldAccentLight,
    secondary = GoldAccentDark,
    onSecondary = DarkLeatherBackground,
    secondaryContainer = DarkParchmentBorder,
    onSecondaryContainer = DarkInkGold,
    background = DarkLeatherBackground,
    onBackground = DarkInkGold,
    surface = DarkParchmentSurface,
    onSurface = DarkInkGold,
    surfaceVariant = DarkParchmentBorder,
    onSurfaceVariant = DarkInkSecondary,
    outline = GoldAccentDark,
    error = WaxRedLight,
    onError = DarkLeatherBackground
)

private val LightDiaryColorScheme = lightColorScheme(
    primary = LeatherBrown,
    onPrimary = ParchmentLight,
    primaryContainer = ParchmentMedium,
    onPrimaryContainer = LeatherDark,
    secondary = GoldAccentDark,
    onSecondary = ParchmentLight,
    secondaryContainer = ParchmentDark,
    onSecondaryContainer = InkSepiaDark,
    background = LeatherDark,
    onBackground = ParchmentLight,
    surface = ParchmentLight,
    onSurface = InkSepiaDark,
    surfaceVariant = ParchmentMedium,
    onSurfaceVariant = InkChestnut,
    outline = GoldAccent,
    error = WaxRed,
    onError = ParchmentLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkDiaryColorScheme else LightDiaryColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiaryTypography,
        content = content
    )
}
