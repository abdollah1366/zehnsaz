package com.example.zehnchin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// Kept for a possible future night mode. The app currently always uses the bright light scheme.
internal val DarkColorScheme = darkColorScheme(
    primary = Violet200,
    onPrimary = Violet900,
    primaryContainer = Violet800,
    onPrimaryContainer = Violet50,
    secondary = Berry200,
    onSecondary = Berry900,
    secondaryContainer = Berry800,
    onSecondaryContainer = Berry50,
    tertiary = Sun200,
    onTertiary = Sun900,
    tertiaryContainer = Sun800,
    onTertiaryContainer = Sun50,
    background = Night950,
    onBackground = Ink50,
    surface = Night900,
    onSurface = Ink50,
    surfaceVariant = Night800,
    onSurfaceVariant = Plum300,
    outline = Plum400,
    error = Error200
)

internal val LightColorScheme = lightColorScheme(
    primary = Violet700,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Violet900,
    secondary = Berry700,
    onSecondary = Color.White,
    secondaryContainer = Berry100,
    onSecondaryContainer = Berry900,
    tertiary = Sun700,
    onTertiary = Color.White,
    tertiaryContainer = Sun100,
    onTertiaryContainer = Sun900,
    background = Cream50,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Lavender100,
    onSurfaceVariant = Plum700,
    surfaceTint = Violet700,
    outline = Plum400,
    outlineVariant = Plum200,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFBFF),
    surfaceContainer = Color(0xFFF8F4FF),
    surfaceContainerHigh = Color(0xFFFDFBFF),
    // Cards use this one: near-white so they stand out on the pastel backdrop.
    surfaceContainerHighest = Color(0xFFFCFAFF),
    error = Error700
)

// Rounded, friendly corners for cards and buttons.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ZehnChinTheme(
    // Always bright by default: the cheerful light palette is part of the app's character.
    darkTheme: Boolean = false,
    // Off by default: the brand palette is part of how the app is recognised.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val feedbackColors = if (darkTheme) DarkFeedbackColors else LightFeedbackColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalFeedbackColors provides feedbackColors
            ) {
                content()
            }
        }
    )
}
