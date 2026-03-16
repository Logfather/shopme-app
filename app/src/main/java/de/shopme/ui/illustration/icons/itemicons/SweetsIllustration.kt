package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun SweetsIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.sweets_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticles(this)
        }

        // Cookie (Left)
        Cookie(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = (-40).dp)
        )

        // Chocolate Bar (Right)
        ChocolateBar(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = (-40).dp)
        )

        // Cupcake (Center)
        Cupcake(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.Center)
                .offset(y = (-10).dp)
        )

        // Lollipop (Bottom Left)
        Lollipop(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.BottomStart)
                .offset(x = 5.dp, y = (-20).dp)
        )

        // Sweets on the ground
        GroundSweets(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Cupcake(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Liner (Light Blue)
        val linerPath = Path().apply {
            moveTo(w * 0.25f, h * 0.55f)
            lineTo(w * 0.75f, h * 0.55f)
            lineTo(w * 0.68f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.95f)
            close()
        }
        drawPath(linerPath, Color(0xFFAEDFF7))
        drawPath(linerPath, Color(0xFF5D8AA8), style = Stroke(width = 2f))
        
        // Liner ridges
        for (i in 1..8) {
            val xTop = w * (0.25f + i * 0.055f)
            val xBottom = w * (0.32f + i * 0.04f)
            drawLine(
                color = Color(0xFF5D8AA8).copy(alpha = 0.5f),
                start = Offset(xTop, h * 0.55f),
                end = Offset(xBottom, h * 0.95f),
                strokeWidth = 1f
            )
        }

        // Cake base (Brown)
        drawOval(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(w * 0.56f, h * 0.2f)
        )

        // Frosting (Pink)
        val frostingPath = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            quadraticTo(w * 0.2f, h * 0.35f, w * 0.5f, h * 0.35f)
            quadraticTo(w * 0.8f, h * 0.35f, w * 0.8f, h * 0.5f)
            close()
        }
        drawPath(frostingPath, Color(0xFFFFC1CC))
        
        // Second layer
        val frostingPath2 = Path().apply {
            moveTo(w * 0.25f, h * 0.4f)
            quadraticTo(w * 0.25f, h * 0.25f, w * 0.5f, h * 0.25f)
            quadraticTo(w * 0.75f, h * 0.25f, w * 0.75f, h * 0.4f)
            close()
        }
        drawPath(frostingPath2, Color(0xFFFFB6C1))
        
        // Third layer
        val frostingPath3 = Path().apply {
            moveTo(w * 0.35f, h * 0.3f)
            quadraticTo(w * 0.35f, h * 0.18f, w * 0.5f, h * 0.18f)
            quadraticTo(w * 0.65f, h * 0.18f, w * 0.65f, h * 0.3f)
            close()
        }
        drawPath(frostingPath3, Color(0xFFFFA6B1))

        // Cherry
        drawCircle(Color(0xFFD32F2F), radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.12f))
        val stemPath = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            quadraticTo(w * 0.45f, h * (-0.15f), w * 0.4f, h * (-0.25f))
        }
        drawPath(stemPath, Color(0xFF388E3C), style = Stroke(width = 4f))

        // Sprinkles
        val sprinkleColors = listOf(Color.Yellow, Color.Cyan, Color.Green, Color.Red, Color.White)
        for (i in 0..15) {
            val angle = i * 23.5f
            val radius = w * (0.2f + (i % 3) * 0.05f)
            val sx = w * 0.5f + Math.cos(angle.toDouble()).toFloat() * radius * 0.6f
            val sy = h * 0.4f + Math.sin(angle.toDouble()).toFloat() * radius * 0.3f
            drawCircle(sprinkleColors[i % sprinkleColors.size], radius = 3f, center = Offset(sx, sy))
        }

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.48f), w * 0.35f)
    }
}

@Composable
private fun Cookie(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(Color(0xFFEBC18D), radius = w / 2, center = center)
        
        // Bite mask (white circle to simulate bite)
        drawCircle(Color.White, radius = w * 0.18f, center = Offset(w * 0.85f, h * 0.15f))
        drawCircle(Color.White, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.05f))

        // Chips
        val chipPositions = listOf(
            Offset(w * 0.2f, h * 0.3f), Offset(w * 0.4f, h * 0.15f),
            Offset(w * 0.15f, h * 0.6f), Offset(w * 0.3f, h * 0.85f),
            Offset(w * 0.7f, h * 0.8f), Offset(w * 0.85f, h * 0.5f)
        )
        chipPositions.forEach { pos ->
            drawCircle(Color(0xFF3E2723), radius = w * 0.08f, center = pos)
        }

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.55f), w * 0.5f)
    }
}

@Composable
private fun ChocolateBar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Chocolate
        val chocolateRect = Rect(w * 0.2f, h * 0.1f, w * 0.8f, h * 0.8f)
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(chocolateRect.left, chocolateRect.top),
            size = Size(chocolateRect.width, chocolateRect.height),
            cornerRadius = CornerRadius(10f, 10f)
        )
        
        // Bite
        drawCircle(Color.White, radius = w * 0.15f, center = Offset(w * 0.8f, h * 0.1f))

        // Chocolate grid
        for (i in 1..2) {
            drawLine(Color(0xFF3E2723), Offset(w * (0.2f + i * 0.2f), h * 0.1f), Offset(w * (0.2f + i * 0.2f), h * 0.8f), strokeWidth = 2f)
        }
        for (i in 1..3) {
            drawLine(Color(0xFF3E2723), Offset(w * 0.2f, h * (0.1f + i * 0.175f)), Offset(w * 0.8f, h * (0.1f + i * 0.175f)), strokeWidth = 2f)
        }

        // Silver Wrapper
        val wrapperPath = Path().apply {
            moveTo(w * 0.15f, h * 0.6f)
            lineTo(w * 0.85f, h * 0.6f)
            lineTo(w * 0.85f, h * 0.95f)
            lineTo(w * 0.15f, h * 0.95f)
            close()
        }
        drawPath(wrapperPath, Color(0xFFB0BEC5))
        
        // Jagged edge
        val edgePath = Path().apply {
            moveTo(w * 0.15f, h * 0.6f)
            for (i in 0..10) {
                val x = w * (0.15f + i * 0.07f)
                val y = if (i % 2 == 0) h * 0.6f else h * 0.55f
                lineTo(x, y)
            }
        }
        drawPath(edgePath, Color(0xFFB0BEC5))

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.4f), w * 0.4f)
    }
}

@Composable
private fun Lollipop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Stick
        drawLine(
            color = Color.White,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.8f, h * 1.2f),
            strokeWidth = 15f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.8f, h * 1.2f),
            strokeWidth = 1f
        )

        // Swirl colors
        val colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color(0xFFFFA500))
        for (i in 0..11) {
            withTransform({
                rotate(i * 30f, pivot = Offset(w * 0.45f, h * 0.45f))
            }) {
                val swirlPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.45f)
                    quadraticTo(w * 0.65f, h * 0.35f, w * 0.85f, h * 0.45f)
                    quadraticTo(w * 0.65f, h * 0.55f, w * 0.45f, h * 0.45f)
                }
                drawPath(swirlPath, colors[i % colors.size])
            }
        }
        
        drawCircle(
            color = Color.Black.copy(alpha = 0.1f),
            radius = w * 0.4f,
            center = Offset(w * 0.45f, h * 0.45f),
            style = Stroke(width = 2f)
        )

        // Face
        drawCuteFace(this, Offset(w * 0.45f, h * 0.45f), w * 0.4f)
    }
}

@Composable
private fun GroundSweets(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Wrapped candies
        val candyColors = listOf(Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFF4CAF50))
        for (i in 0..2) {
            val cx = w * (0.2f + i * 0.3f)
            val cy = h * 0.85f
            
            // Wrapper ends
            val leftEnd = Path().apply {
                moveTo(cx - 30f, cy)
                lineTo(cx - 50f, cy - 20f)
                lineTo(cx - 50f, cy + 20f)
                close()
            }
            drawPath(leftEnd, candyColors[i])
            val rightEnd = Path().apply {
                moveTo(cx + 30f, cy)
                lineTo(cx + 50f, cy - 20f)
                lineTo(cx + 50f, cy + 20f)
                close()
            }
            drawPath(rightEnd, candyColors[i])
            
            // Candy body
            drawRoundRect(
                color = candyColors[i],
                topLeft = Offset(cx - 30f, cy - 20f),
                size = Size(60f, 40f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            
            if (i == 0) { // Add face to the purple one
                drawCuteFace(this, Offset(cx, cy), 50f)
            }
        }

        // Small candies/beans
        val beanColors = listOf(Color(0xFFFFC0CB), Color.Cyan, Color.Yellow)
        for (i in 0..5) {
            val bx = w * (0.1f + i * 0.15f)
            val by = h * (0.92f + (i % 2) * 0.03f)
            drawOval(
                color = beanColors[i % beanColors.size],
                topLeft = Offset(bx, by),
                size = Size(30f, 20f)
            )
        }
    }
}

private fun drawParticles(drawScope: DrawScope) {
    with(drawScope) {
        val w = size.width
        val h = size.height
        val colors = listOf(Color.Yellow, Color.Cyan, Color.Magenta, Color.Green, Color.Red)
        for (i in 0..40) {
            val px = w * (i * 0.17f % 1f)
            val py = h * (i * 0.23f % 1f)
            if (i % 5 == 0) {
                // Star
                drawStar(Offset(px, py), 10f, colors[i % colors.size])
            } else {
                // Dot
                drawCircle(colors[i % colors.size], radius = 3f, center = Offset(px, py))
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        for (i in 0..4) {
            val angle = i * 72f - 90f
            val rad = Math.toRadians(angle.toDouble())
            val x = center.x + Math.cos(rad).toFloat() * size
            val y = center.y + Math.sin(rad).toFloat() * size
            if (i == 0) moveTo(x, y) else lineTo(x, y)
            
            val nextAngle = angle + 36f
            val nextRad = Math.toRadians(nextAngle.toDouble())
            val nx = center.x + Math.cos(nextRad).toFloat() * size * 0.4f
            val ny = center.y + Math.sin(nextRad).toFloat() * size * 0.4f
            lineTo(nx, ny)
        }
        close()
    }
    drawPath(path, color)
}

private fun drawCuteFace(drawScope: DrawScope, center: Offset, width: Float) {
    with(drawScope) {
        val eyeSize = width * 0.3f
        val eyeOffset = width * 0.25f
        
        // Eyes
        drawCircle(Color.White, radius = eyeSize / 2, center = Offset(center.x - eyeOffset, center.y - 10f))
        drawCircle(Color.Black, radius = eyeSize / 5, center = Offset(center.x - eyeOffset + 2f, center.y - 8f))
        drawCircle(Color.Black, radius = eyeSize / 2, center = Offset(center.x - eyeOffset, center.y - 10f), style = Stroke(width = 1.5f))
        
        drawCircle(Color.White, radius = eyeSize / 2, center = Offset(center.x + eyeOffset, center.y - 10f))
        drawCircle(Color.Black, radius = eyeSize / 5, center = Offset(center.x + eyeOffset - 2f, center.y - 8f))
        drawCircle(Color.Black, radius = eyeSize / 2, center = Offset(center.x + eyeOffset, center.y - 10f), style = Stroke(width = 1.5f))

        // Mouth
        val mouthWidth = width * 0.4f
        val mouthHeight = width * 0.2f
        val mouthRect = Rect(center.x - mouthWidth / 2, center.y + 5f, center.x + mouthWidth / 2, center.y + 5f + mouthHeight)
        val mouthPath = Path().apply {
            moveTo(mouthRect.left, mouthRect.top)
            quadraticTo(center.x, mouthRect.bottom + 10f, mouthRect.right, mouthRect.top)
            quadraticTo(center.x, mouthRect.top + 3f, mouthRect.left, mouthRect.top)
            close()
        }
        drawPath(mouthPath, Color(0xFF212121))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(center.x - mouthWidth * 0.3f, mouthRect.bottom - 5f)
            quadraticTo(center.x, mouthRect.bottom + 5f, center.x + mouthWidth * 0.3f, mouthRect.bottom - 5f)
        }
        drawPath(tonguePath, Color(0xFFFF5252), style = Stroke(width = 4f))
    }
}

@Preview(showBackground = true)
@Composable
private fun SweetsIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SweetsIllustration()
        }
    }
}
