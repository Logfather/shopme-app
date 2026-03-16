package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun BreadIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.bread_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground shadow/base
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
                size = Size(size.width * 0.8f, size.height * 0.2f)
            )
        }

        // Bread Loaf
        Bread(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.TopCenter)
                .offset(y = 20.dp, x = (-10).dp)
        )

        // Basket/Tray (Lower part)
        Basket(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .align(Alignment.BottomCenter)
                .offset(y = (-15).dp)
        )

        // Cloth
        Cloth(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = (-35).dp)
        )

        // Green Beans
        GreenBeans(
            modifier = Modifier
                .fillMaxSize(0.25f)
                .align(Alignment.BottomEnd)
                .offset(x = (-15).dp, y = (-30).dp)
        )

        // Sparkles and Particles
        Sparkles(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Bread(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val breadColorMain = Color(0xFFFFD54F)
        val breadColorDark = Color(0xFFF57C00)
        val outlineColor = Color(0xFF5D4037)

        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.75f)
            cubicTo(
                size.width * 0.1f, size.height * 0.3f,
                size.width * 0.4f, size.height * 0.05f,
                size.width * 0.7f, size.height * 0.1f
            )
            cubicTo(
                size.width * 0.95f, size.height * 0.2f,
                size.width * 1.0f, size.height * 0.6f,
                size.width * 0.85f, size.height * 0.8f
            )
            cubicTo(
                size.width * 0.7f, size.height * 0.95f,
                size.width * 0.3f, size.height * 0.9f,
                size.width * 0.15f, size.height * 0.75f
            )
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(breadColorMain, breadColorDark),
                center = Offset(size.width * 0.5f, size.height * 0.4f),
                radius = size.width * 0.7f
            )
        )
        drawPath(path = path, color = outlineColor, style = Stroke(width = 4f))

        // Slits
        for (i in 0..2) {
            val startX = size.width * (0.3f + i * 0.2f)
            val startY = size.height * (0.55f - i * 0.15f)
            val endX = size.width * (0.5f + i * 0.2f)
            val endY = size.height * (0.8f - i * 0.15f)

            val slitPath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(
                    (startX + endX) / 2f + 15f, (startY + endY) / 2f,
                    endX, endY
                )
            }
            drawPath(slitPath, outlineColor, style = Stroke(width = 4f))
        }
    }
}

@Composable
private fun Basket(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val basketColor = Color(0xFFFDF5E6)
        val basketOutline = Color(0xFF8B4513)

        // Main tray body with compartments
        val path = Path().apply {
            moveTo(size.width * 0.05f, size.height * 0.55f)
            lineTo(size.width * 0.95f, size.height * 0.55f)
            cubicTo(
                size.width * 0.95f, size.height * 0.85f,
                size.width * 0.8f, size.height * 0.95f,
                size.width * 0.5f, size.height * 0.95f
            )
            cubicTo(
                size.width * 0.2f, size.height * 0.95f,
                size.width * 0.05f, size.height * 0.85f,
                size.width * 0.05f, size.height * 0.55f
            )
            close()
        }

        drawPath(path, basketColor)
        drawPath(path, basketOutline, style = Stroke(width = 4f))

        // Compartment lines (scalloped look)
        for (i in 1..3) {
            val x = size.width * (0.25f * i)
            val cPath = Path().apply {
                moveTo(x, size.height * 0.6f)
                cubicTo(
                    x - 20f, size.height * 0.85f,
                    x + 20f, size.height * 0.85f,
                    x, size.height * 0.95f
                )
            }
            drawPath(cPath, basketOutline.copy(alpha = 0.5f), style = Stroke(width = 2f))
        }
    }
}

@Composable
private fun Cloth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val clothColor = Color(0xFFFAFAD2)
        val clothOutline = Color(0xFFDAA520)

        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.3f)
            cubicTo(
                size.width * 0.4f, size.height * 0.1f,
                size.width * 0.9f, size.height * 0.1f,
                size.width * 1.1f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.9f, size.height * 0.9f,
                size.width * 0.4f, size.height * 0.85f,
                size.width * 0.1f, size.height * 0.6f
            )
            close()
        }

        drawPath(path, clothColor)
        drawPath(path, clothOutline, style = Stroke(width = 3f))

        // Fold lines
        val foldPath = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.3f)
            cubicTo(
                size.width * 0.6f, size.height * 0.4f,
                size.width * 0.5f, size.height * 0.7f,
                size.width * 0.8f, size.height * 0.75f
            )
        }
        drawPath(foldPath, clothOutline.copy(alpha = 0.6f), style = Stroke(width = 2f))
    }
}

@Composable
private fun GreenBeans(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val beanColor = Color(0xFF7CB342)
        val beanOutline = Color(0xFF33691E)

        // Two oval beans
        drawOval(
            color = beanColor,
            topLeft = Offset(size.width * 0.1f, size.height * 0.4f),
            size = Size(size.width * 0.5f, size.height * 0.4f)
        )
        drawOval(
            color = beanOutline,
            topLeft = Offset(size.width * 0.1f, size.height * 0.4f),
            size = Size(size.width * 0.5f, size.height * 0.4f),
            style = Stroke(width = 3f)
        )

        drawOval(
            color = beanColor,
            topLeft = Offset(size.width * 0.4f, size.height * 0.2f),
            size = Size(size.width * 0.6f, size.height * 0.5f)
        )
        drawOval(
            color = beanOutline,
            topLeft = Offset(size.width * 0.4f, size.height * 0.2f),
            size = Size(size.width * 0.6f, size.height * 0.5f),
            style = Stroke(width = 3f)
        )
    }
}

@Composable
private fun Sparkles(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sparkleColor = Color(0xFFFFD54F)
        
        drawCircle(sparkleColor, radius = 7f, center = Offset(size.width * 0.38f, size.height * 0.22f))
        drawCircle(sparkleColor, radius = 5f, center = Offset(size.width * 0.33f, size.height * 0.28f))
        drawCircle(sparkleColor, radius = 4f, center = Offset(size.width * 0.82f, size.height * 0.35f))
        drawCircle(sparkleColor, radius = 6f, center = Offset(size.width * 0.85f, size.height * 0.42f))
        
        // Arc-like sparkle
        drawArc(
            color = sparkleColor,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(size.width * 0.1f, size.height * 0.2f),
            size = Size(size.width * 0.15f, size.height * 0.15f),
            style = Stroke(width = 4f)
        )

        drawArc(
            color = sparkleColor,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(size.width * 0.85f, size.height * 0.45f),
            size = Size(size.width * 0.1f, size.height * 0.1f),
            style = Stroke(width = 3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BreadIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            BreadIllustration(modifier = Modifier.fillMaxSize())
        }
    }
}
