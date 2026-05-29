package de.shopme.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(

    // Primary
    primary = BrandGreen,
    onPrimary = BrandWhite,

    // Secondary
    secondary = BrandOlive,
    onSecondary = BrandWhite,

    // Background
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF2E2E2E),

    // Surface (Cards etc.)
    surface = BrandWhite,
    onSurface = Color(0xFF2E2E2E),

    // Accent / Highlight
    tertiary = Color(0xFFFFB74D),
    onTertiary = BrandBlack,

    // Error
    error = Color(0xFFD32F2F),
    onError = BrandWhite
)

private val DarkColorScheme = darkColorScheme(

    primary = BrandGreen,
    onPrimary = BrandBlack,

    secondary = BrandOlive,
    onSecondary = BrandBlack,

    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFEDEDED),

    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFEDEDED),

    tertiary = Color(0xFFFFB74D),
    onTertiary = BrandBlack,

    error = Color(0xFFEF5350),
    onError = BrandBlack
)

@Composable
fun HivraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShopMeTypography,
        shapes = ShopMeShapes,
        content = content
    )
}