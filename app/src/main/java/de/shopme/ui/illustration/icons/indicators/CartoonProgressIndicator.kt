package de.shopme.ui.illustration.icons.indicators


import androidx.compose.animation.animateColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandOlive
import kotlin.math.*

@Composable
fun CartoonProgressIndicator(
    progress: Float, // 0f - 1f
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {

    val infiniteTransition = rememberInfiniteTransition()

    // ============================================================
    // 🔄 Rotation (Spinner Effekt)
    // ============================================================
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // ============================================================
    // 🎨 Farbwechsel
    // ============================================================
    val color by infiniteTransition.animateColor(
        initialValue = BrandGreen,
        targetValue = BrandOlive,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorAnim"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {

        val strokeWidth = size.toPx() * 0.08f
        val radius = size.toPx() / 2

        // ============================================================
        // Hintergrund
        // ============================================================
        drawCircle(
            color = Color(0xFFEDE0D4),
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        // ============================================================
        // Fortschritt / Arc
        // ============================================================
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )

        // ============================================================
        // ✨ Krümel (folgt Progress + Rotation)
        // ============================================================
        val angle = Math.toRadians((rotation + 360 * progress - 90).toDouble())

        val crumbRadius = radius - strokeWidth / 2

        val crumbX = center.x + cos(angle).toFloat() * crumbRadius
        val crumbY = center.y + sin(angle).toFloat() * crumbRadius

        drawCircle(
            color = color.copy(alpha = 0.9f),
            radius = strokeWidth * 0.6f,
            center = Offset(crumbX, crumbY)
        )
    }
}