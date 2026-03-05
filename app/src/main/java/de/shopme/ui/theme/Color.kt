package de.shopme.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand
val BrandGreen = Color(0xFF809C13)
val BrandOlive = Color(0xFF939176)

// Dark Base
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)

// Light Base
private val LightBackground = Color(0xFFFDFDFD)
private val LightSurface = Color(0xFFFFFFFF)

val DarkColorScheme = darkColorScheme(
    primary = BrandGreen,
    secondary = BrandOlive,
    tertiary = BrandGreen,

    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2A2A2A),

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,

    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),

    outline = Color(0xFF3A3A3A)
)

val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    secondary = BrandOlive,
    tertiary = BrandGreen,

    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFEDEDED),

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,

    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF5A5A5A),

    outline = Color(0xFFD0D0D0)
)

// Domain colors (NICHT Teil des ColorScheme)
val CategoryColors: Map<String, Color> = mapOf(
    "Obst & Gemüse" to Color(0xFF4C9141),
    "Fleisch & Wurst" to Color(0xFFA42C00),
    "Fisch & Meeresfrüchte" to Color(0xFF0153A4),
    "Molkereiprodukte & Eier" to Color(0xFFFCE49C),
    "Backwaren" to Color(0xFFFF9800),
    "Backzutaten & Backmischungen" to Color(0xFF79553C),
    "Tiefkühlprodukte" to Color(0xFFF8FCE8),
    "Konserven & Fertiggerichte" to Color(0xFFD8E79C),
    "Nudeln, Reis & Getreide" to Color(0xFFFF8800),
    "Gewürze & Öle" to Color(0xFF909090),
    "Saucen & Aufstriche" to Color(0xFF007020),
    "Snacks & Süßwaren" to Color(0xFF846028),
    "Getränke" to Color(0xFFDC562C),
    "Kaffee & Tee" to Color(0xFF76541C),
    "Sonstiges" to Color(0xFFA0A0B0)
)