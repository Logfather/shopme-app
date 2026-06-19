package de.shopme.ui.theme

import androidx.compose.ui.graphics.Color

// Brand
val BrandGreen = Color(0xFF809C13)
val BrandOlive = Color(0xFF939176)
val BrandBlack = Color.Black
val BrandWhite = Color.White
val BrandRed = Color(0xFF0000)
val BrandCreme = Color(0xFFF2F0EF)

val BrandGrey = Color(0xFF575D5E)

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

object NutriScoreColors {

    val A = Color(0xFF1B8F3A)
    val B = Color(0xFF7CB342)
    val C = Color(0xFFFBC02D)
    val D = Color(0xFFF57C00)
    val E = Color(0xFFD32F2F)

    fun fromScore(score: String): Color =
        when (score.uppercase()) {
            "A" -> A
            "B" -> B
            "C" -> C
            "D" -> D
            "E" -> E
            else -> Color.Gray
        }
}