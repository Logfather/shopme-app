package de.shopme.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.tooling.preview.Preview
import de.shopme.R

/* =========================================================
   COLOR DEFINITIONS
   ========================================================= */

// --------------------
// Nimblu (CORE / SYSTEM)
// --------------------
private val NimbluDarkColors = darkColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF38BDF8),
    background = Color(0xFF020617),
    surface = Color(0xFF0F172A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
    error = Color(0xFFEF4444)
)

private val NimbluLightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF38BDF8),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    error = Color(0xFFEF4444)
)

// --------------------
// Hivra (UX LAYER)
// --------------------
val HivraOverlayColors = lightColorScheme(
    primary = Color(0xFF809C13),
    onPrimary = Color.White,

    secondary = Color(0xFF939176),
    onSecondary = Color.White,

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF2E2E2E),

    surface = Color(0xFFF7F7F7),
    onSurface = Color(0xFF2E2E2E),

    error = Color(0xFFEF4444),
    onError = Color.White
)

/* =========================================================
   TYPOGRAPHY
   ========================================================= */

private val Nunito = FontFamily(
    Font(resId = R.font.nunito_regular, weight = FontWeight.Normal),
    Font(resId = R.font.nunito_medium, weight = FontWeight.Medium),
    Font(resId = R.font.nunito_bold, weight = FontWeight.Bold)
)

// --------------------
// Nimblu (System Typography)
// --------------------
val NimbluTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

// --------------------
// Hivra (UX Typography)
// --------------------
val HivraTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    )
)

/* =========================================================
   SHAPES
   ========================================================= */

val NimbluShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

val HivraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp)
)

/* =========================================================
   THEMES
   ========================================================= */

// --------------------
// CORE THEME (Nimblu)
// --------------------
@Composable
fun NimbluCoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) NimbluDarkColors else NimbluLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = NimbluTypography,
        shapes = NimbluShapes,
        content = content
    )
}

// --------------------
// UX LAYER (Hivra)
// --------------------
@Composable
fun HivraUXLayer(
    content: @Composable () -> Unit
) {
    val base = MaterialTheme.colorScheme

    val merged = base.copy(
        primary = HivraOverlayColors.primary,
        secondary = HivraOverlayColors.secondary,
        background = HivraOverlayColors.background,
        surface = HivraOverlayColors.surface,
        onPrimary = HivraOverlayColors.onPrimary,
        onSurface = HivraOverlayColors.onSurface
    )

    MaterialTheme(
        colorScheme = merged,
        typography = HivraTypography,
        shapes = HivraShapes,
        content = content
    )
}

/* =========================================================
   ROOT WRAPPER
   ========================================================= */

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    NimbluCoreTheme {
        HivraUXLayer {
            content()
        }
    }
}

/* =========================================================
   PREVIEWS
   ========================================================= */

@Composable
@Preview(name = "Nimblu Core", showBackground = true)
fun PreviewNimbluCore() {
    NimbluCoreTheme {
        Surface {
            Text("Nimblu Core Layer")
        }
    }
}

@Composable
@Preview(name = "Hivra UX Layer", showBackground = true)
fun PreviewHivraLayer() {
    NimbluCoreTheme {
        HivraUXLayer {
            Surface {
                Button(onClick = {}) {
                    Text("Hivra Button")
                }
            }
        }
    }
}

@Composable
@Preview(name = "Full App Theme", showBackground = true)
fun PreviewFullTheme() {
    AppTheme {
        Surface {
            Column {
                Text("Headline", style = MaterialTheme.typography.headlineLarge)
                Button(onClick = {}) {
                    Text("Item hinzufügen")
                }
            }
        }
    }
}