package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun BakeryIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.bakery_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Wheat Stalks Background
        WheatStalks(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.Center)
                .offset(y = (-40).dp)
        )

        // Baguette
        Baguette(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
        )

        // Donut
        Donut(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-20).dp)
        )

        // Muffin
        Muffin(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 20.dp)
        )

        // Croissant
        Croissant(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp, x = 20.dp)
        )

        // Crumbs and Seeds
        Crumbs(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Baguette(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.3f, size.height)
                lineTo(size.width * 0.3f, size.height * 0.2f)
                quadraticTo(size.width * 0.3f, 0f, size.width * 0.5f, 0f)
                quadraticTo(size.width * 0.7f, 0f, size.width * 0.7f, size.height * 0.2f)
                lineTo(size.width * 0.7f, size.height)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFDEB887), Color(0xFFD2691E))
                )
            )
            drawPath(path = path, color = Color(0xFF5D4037), style = Stroke(width = 4f))

            // Texture lines
            for (i in 1..4) {
                val y = size.height * 0.2f * i
                drawArc(
                    color = Color(0xFF8D6E63),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.35f, y),
                    size = Size(size.width * 0.3f, size.height * 0.05f),
                    style = Stroke(width = 3f)
                )
            }
        }
        CharacterFace(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .offset(y = 20.dp),
            eyeSize = 12.dp
        )
    }
}

@Composable
private fun Donut(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.5f

            // Dough
            drawCircle(
                color = Color(0xFFF5DEB3),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color(0xFF5D4037),
                radius = radius,
                center = center,
                style = Stroke(width = 4f)
            )

            // Chocolate Glaze
            drawCircle(
                color = Color(0xFF4B2C20),
                radius = radius * 0.85f,
                center = center
            )

            // Hole
            drawCircle(
                color = Color.White,
                radius = radius * 0.3f,
                center = center
            )
            drawCircle(
                color = Color(0xFF5D4037),
                radius = radius * 0.3f,
                center = center,
                style = Stroke(width = 4f)
            )

            // Sprinkles
            val sprinkleColors = listOf(Color.Red, Color.Yellow, Color.Cyan, Color.Green, Color.Magenta)
            for (i in 0..15) {
                val angle = (i * 24).toDouble()
                val dist = radius * 0.6f
                val x = center.x + Math.cos(Math.toRadians(angle)).toFloat() * dist
                val y = center.y + Math.sin(Math.toRadians(angle)).toFloat() * dist
                drawRoundRect(
                    color = sprinkleColors[i % sprinkleColors.size],
                    topLeft = Offset(x, y),
                    size = Size(8f, 4f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
        // Eyes inside/near the hole
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CharacterEye(eyeSize = 10.dp)
            CharacterEye(eyeSize = 10.dp)
        }
    }
}

@Composable
private fun Muffin(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Liner (trapezoid)
            val linerPath = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.9f)
                lineTo(size.width * 0.7f, size.height * 0.9f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                close()
            }
            drawPath(linerPath, Color(0xFFE1F5FE))
            drawPath(linerPath, Color(0xFF0288D1), style = Stroke(width = 4f))

            // Liner stripes
            for (i in 1..5) {
                val x = size.width * (0.2f + i * 0.1f)
                drawLine(
                    color = Color(0xFF0288D1),
                    start = Offset(x, size.height * 0.5f),
                    end = Offset(size.width * (0.3f + (i - 1) * 0.1f), size.height * 0.9f),
                    strokeWidth = 2f
                )
            }

            // Muffin top
            drawOval(
                color = Color(0xFFF5DEB3),
                topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
                size = Size(size.width * 0.7f, size.height * 0.35f)
            )
            drawOval(
                color = Color(0xFF5D4037),
                topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
                size = Size(size.width * 0.7f, size.height * 0.35f),
                style = Stroke(width = 4f)
            )

            // Blueberries
            val blueberryColor = Color(0xFF1A237E)
            drawCircle(blueberryColor, radius = 8f, center = Offset(size.width * 0.3f, size.height * 0.4f))
            drawCircle(blueberryColor, radius = 8f, center = Offset(size.width * 0.5f, size.height * 0.35f))
            drawCircle(blueberryColor, radius = 8f, center = Offset(size.width * 0.7f, size.height * 0.42f))
        }
        CharacterFace(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .offset(y = (-5).dp),
            eyeSize = 12.dp
        )
    }
}

@Composable
private fun Croissant(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Replicating a crescent-like layered shape
            val center = Offset(size.width / 2, size.height / 2)
            val path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.6f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.2f,
                    size.width * 0.7f, size.height * 0.2f,
                    size.width * 0.9f, size.height * 0.6f
                )
                cubicTo(
                    size.width * 0.7f, size.height * 0.8f,
                    size.width * 0.3f, size.height * 0.8f,
                    size.width * 0.1f, size.height * 0.6f
                )
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF4A460), Color(0xFFD2691E)),
                    center = center
                )
            )
            drawPath(path = path, color = Color(0xFF5D4037), style = Stroke(width = 4f))

            // Layers
            for (i in 1..4) {
                val xFactor = i * 0.15f
                drawArc(
                    color = Color(0xFF8D6E63),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * (0.15f + xFactor), size.height * 0.4f),
                    size = Size(size.width * 0.15f, size.height * 0.2f),
                    style = Stroke(width = 2f)
                )
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.4f),
            eyeSize = 14.dp
        )
    }
}

@Composable
private fun WheatStalks(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stalkColor = Color(0xFFDAA520)
        // Left Stalk
        drawWheat(Offset(size.width * 0.2f, size.height), stalkColor, -20f)
        // Right Stalk
        drawWheat(Offset(size.width * 0.8f, size.height), stalkColor, 20f)
    }
}

private fun DrawScope.drawWheat(base: Offset, color: Color, angle: Float) {
    val height = size.height * 0.8f
    val path = Path().apply {
        moveTo(base.x, base.y)
        lineTo(base.x, base.y - height)
    }
    drawPath(path, color, style = Stroke(width = 4f))

    for (i in 0..6) {
        val y = base.y - height * (i / 6f)
        drawOval(
            color = color,
            topLeft = Offset(base.x - 10f, y),
            size = Size(20f, 10f)
        )
    }
}

@Composable
private fun Crumbs(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val crumbColor = Color(0xFFDEB887)
        drawCircle(crumbColor, radius = 4f, center = Offset(size.width * 0.1f, size.height * 0.9f))
        drawCircle(crumbColor, radius = 3f, center = Offset(size.width * 0.85f, size.height * 0.85f))
        drawCircle(crumbColor, radius = 5f, center = Offset(size.width * 0.5f, size.height * 0.95f))
        drawCircle(crumbColor, radius = 3f, center = Offset(size.width * 0.2f, size.height * 0.1f))
    }
}

@Composable
private fun CharacterFace(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 16.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CharacterEye(eyeSize = eyeSize)
            CharacterEye(eyeSize = eyeSize)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Mouth(modifier = Modifier.size(eyeSize * 1.5f))
    }
}

@Composable
private fun CharacterEye(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(eyeSize)
            .background(Color.White, CircleShape)
            .border(1.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension * 0.3f,
                center = center.copy(x = center.x + size.width * 0.1f, y = center.y + size.height * 0.1f)
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.1f,
                center = center.copy(x = center.x + size.width * 0.2f, y = center.y - size.height * 0.1f)
            )
        }
    }
}

@Composable
private fun Mouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width / 2f, size.height, size.width, size.height * 0.2f)
            close()
        }
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, Color.Black, style = Stroke(width = 2f))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.7f)
            quadraticTo(size.width / 2f, size.height * 0.9f, size.width * 0.7f, size.height * 0.7f)
        }
        drawPath(tonguePath, Color(0xFFFFC0CB))
    }
}

@Preview(showBackground = true)
@Composable
private fun BakeryIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            BakeryIllustration(modifier = Modifier.size(400.dp))
        }
    }
}
