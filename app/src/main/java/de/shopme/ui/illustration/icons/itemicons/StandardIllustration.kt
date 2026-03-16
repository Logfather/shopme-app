package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme
import java.util.Random

@Composable
fun StandardBadgeIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.standard_badge_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Background Glow/Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE082).copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = center
            )
        }

        // Leaves in background
        BackgroundLeaves(modifier = Modifier.fillMaxSize(0.8f))

        // Small Sparkles and Dots
        Sparkles(modifier = Modifier.fillMaxSize())

        // Stars
        StarsLayer(modifier = Modifier.fillMaxSize())

        // Golden Checkmark
        GoldenCheckmark(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .offset(y = (-10).dp)
        )

        // Crown
        CrownIllustration(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopCenter)
                .offset(y = 40.dp, x = 10.dp)
        )

        // Ribbon
        StandardRibbon(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun BackgroundLeaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val leafColor = Color(0xFF689F38)
        val leafOutline = Color(0xFF1B5E20)

        // Draw several leaves scattered behind the checkmark
        drawLeaf(Offset(size.width * 0.25f, size.height * 0.65f), 45f, leafColor, leafOutline)
        drawLeaf(Offset(size.width * 0.35f, size.height * 0.75f), -15f, leafColor, leafOutline)
        drawLeaf(Offset(size.width * 0.65f, size.height * 0.75f), 30f, leafColor, leafOutline)
        drawLeaf(Offset(size.width * 0.75f, size.height * 0.6f), -45f, leafColor, leafOutline)
        drawLeaf(Offset(size.width * 0.5f, size.height * 0.85f), 10f, leafColor, leafOutline)
    }
}

private fun DrawScope.drawLeaf(center: Offset, rotation: Float, color: Color, outline: Color) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - 30f)
            quadraticTo(center.x + 25f, center.y, center.x, center.y + 30f)
            quadraticTo(center.x - 25f, center.y, center.x, center.y - 30f)
            close()
        }
        drawPath(path, color)
        drawPath(path, outline, style = Stroke(width = 3f))
        drawLine(outline, center.copy(y = center.y - 25f), center.copy(y = center.y + 25f), strokeWidth = 2f)
    }
}

@Composable
private fun GoldenCheckmark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val goldGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFE65100))
        )
        val outlineColor = Color(0xFF3E2723)

        val checkPath = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.55f)
            lineTo(size.width * 0.45f, size.height * 0.85f)
            lineTo(size.width * 0.95f, size.height * 0.25f)
            lineTo(size.width * 0.85f, size.height * 0.15f)
            lineTo(size.width * 0.45f, size.height * 0.65f)
            lineTo(size.width * 0.25f, size.height * 0.45f)
            close()
        }

        // Draw main body with gradient
        drawPath(checkPath, goldGradient)
        // Outline
        drawPath(checkPath, outlineColor, style = Stroke(width = 8f, cap = StrokeCap.Round))

        // Highlight
        val highlightPath = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.68f)
            lineTo(size.width * 0.88f, size.height * 0.22f)
        }
        drawPath(highlightPath, Color.White.copy(alpha = 0.5f), style = Stroke(width = 12f, cap = StrokeCap.Round))
    }
}

@Composable
private fun CrownIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val goldBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
        )
        val outlineColor = Color(0xFF3E2723)

        val crownPath = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.8f)
            lineTo(size.width * 0.9f, size.height * 0.8f)
            lineTo(size.width * 0.95f, size.height * 0.3f)
            lineTo(size.width * 0.75f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.25f, size.height * 0.5f)
            lineTo(size.width * 0.05f, size.height * 0.3f)
            close()
        }

        drawPath(crownPath, goldBrush)
        drawPath(crownPath, outlineColor, style = Stroke(width = 6f))

        // Base of crown
        drawRect(
            color = Color(0xFFFF8F00),
            topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
            size = Size(size.width * 0.8f, size.height * 0.1f)
        )
        drawRect(
            color = outlineColor,
            topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
            size = Size(size.width * 0.8f, size.height * 0.1f),
            style = Stroke(width = 4f)
        )

        // Gems
        drawCircle(Color(0xFFD32F2F), radius = 10f, center = Offset(size.width * 0.5f, size.height * 0.65f)) // Center Red
        drawCircle(outlineColor, radius = 10f, center = Offset(size.width * 0.5f, size.height * 0.65f), style = Stroke(width = 3f))

        drawCircle(Color(0xFF1976D2), radius = 8f, center = Offset(size.width * 0.25f, size.height * 0.65f)) // Left Blue
        drawCircle(outlineColor, radius = 8f, center = Offset(size.width * 0.25f, size.height * 0.65f), style = Stroke(width = 3f))

        drawCircle(Color(0xFF1976D2), radius = 8f, center = Offset(size.width * 0.75f, size.height * 0.65f)) // Right Blue
        drawCircle(outlineColor, radius = 8f, center = Offset(size.width * 0.75f, size.height * 0.65f), style = Stroke(width = 3f))
    }
}

@Composable
private fun StandardRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ribbonColor = Brush.verticalGradient(
                colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
            )
            val outlineColor = Color(0xFF0D47A1)
            val goldTrim = Color(0xFFFFD54F)

            // Ribbon tails (left and right)
            val leftTail = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.65f)
                lineTo(size.width * 0.05f, size.height * 0.55f)
                lineTo(size.width * 0.1f, size.height * 0.75f)
                lineTo(size.width * 0.25f, size.height * 0.75f)
                close()
            }
            drawPath(leftTail, ribbonColor)
            drawPath(leftTail, outlineColor, style = Stroke(width = 4f))

            val rightTail = Path().apply {
                moveTo(size.width * 0.85f, size.height * 0.65f)
                lineTo(size.width * 0.95f, size.height * 0.55f)
                lineTo(size.width * 0.9f, size.height * 0.75f)
                lineTo(size.width * 0.75f, size.height * 0.75f)
                close()
            }
            drawPath(rightTail, ribbonColor)
            drawPath(rightTail, outlineColor, style = Stroke(width = 4f))

            // Main ribbon body
            val ribbonBody = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.65f)
                quadraticTo(size.width * 0.5f, size.height * 0.75f, size.width * 0.85f, size.height * 0.65f)
                lineTo(size.width * 0.85f, size.height * 0.85f)
                quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.15f, size.height * 0.85f)
                close()
            }
            drawPath(ribbonBody, ribbonColor)
            drawPath(ribbonBody, outlineColor, style = Stroke(width = 6f))

            // Gold trim
            drawPath(ribbonBody, goldTrim, style = Stroke(width = 3f))
        }

        Text(
            text = stringResource(R.string.standard_label),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 28.dp)
        )
    }
}

@Composable
private fun StarsLayer(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawStar(Offset(size.width * 0.15f, size.height * 0.25f), 30f)
        drawStar(Offset(size.width * 0.22f, size.height * 0.4f), 20f)
        drawStar(Offset(size.width * 0.8f, size.height * 0.45f), 25f)
        drawStar(Offset(size.width * 0.72f, size.height * 0.55f), 15f)
    }
}

private fun DrawScope.drawStar(center: Offset, size: Float) {
    val starColor = Color(0xFFFFB300)
    val outlineColor = Color(0xFF3E2723)
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.3f, center.y - size * 0.3f)
        lineTo(center.x + size, center.y)
        lineTo(center.x + size * 0.3f, center.y + size * 0.3f)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.3f, center.y + size * 0.3f)
        lineTo(center.x - size, center.y)
        lineTo(center.x - size * 0.3f, center.y - size * 0.3f)
        close()
    }
    drawPath(path, starColor)
    drawPath(path, outlineColor, style = Stroke(width = 3f))
}

@Composable
private fun Sparkles(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val random = Random(123)
        repeat(15) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = 2f + random.nextFloat() * 4f
            val color = if (random.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFE65100)
            drawCircle(color, radius, Offset(x, y))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StandardBadgeIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            StandardBadgeIllustration()
        }
    }
}
