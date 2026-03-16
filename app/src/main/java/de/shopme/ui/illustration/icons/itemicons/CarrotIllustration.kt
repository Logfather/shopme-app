package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun CarrotIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.carrot_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground shadow/base
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .align(Alignment.BottomCenter)
                .offset(y = (-5).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3),
                topLeft = Offset(size.width * 0.1f, size.height * 0.88f),
                size = Size(size.width * 0.8f, size.height * 0.12f)
            )
        }

        // Bottom Green Base (Cabbage-like leaves)
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.7f)
                .align(Alignment.BottomCenter)
                .offset(y = (-15).dp)
        ) {
            drawLeaf(Offset(size.width * 0.3f, size.height * 0.8f), size.width * 0.4f)
            drawLeaf(Offset(size.width * 0.7f, size.height * 0.8f), size.width * 0.45f)
            drawLeaf(Offset(size.width * 0.5f, size.height * 0.9f), size.width * 0.5f)
        }

        // Carrot Body and Top Leaves
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .align(Alignment.Center)
                .graphicsLayer(rotationZ = 15f)
                .offset(y = (-10).dp)
        ) {
            // Top Leaves
            Canvas(
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .align(Alignment.TopCenter)
                    .offset(y = 10.dp)
            ) {
                drawCarrotTop(size)
            }

            // Carrot Body
            Canvas(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-5).dp)
            ) {
                val carrotPath = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.1f)
                    quadraticTo(size.width * 0.5f, size.height * 0.05f, size.width * 0.8f, size.height * 0.1f)
                    quadraticTo(size.width * 0.95f, size.height * 0.6f, size.width * 0.65f, size.height * 0.95f)
                    quadraticTo(size.width * 0.5f, size.height * 1.05f, size.width * 0.35f, size.height * 0.95f)
                    quadraticTo(size.width * 0.05f, size.height * 0.6f, size.width * 0.2f, size.height * 0.1f)
                }
                
                drawPath(
                    path = carrotPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
                    )
                )
                drawPath(
                    path = carrotPath,
                    color = Color(0xFF4E342E),
                    style = Stroke(width = 4f)
                )

                // Texture lines
                drawCarrotLines(size)
            }

            // Face
            Box(
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .align(Alignment.Center)
                    .offset(y = 10.dp, x = 5.dp)
            ) {
                // Left Eye
                CarrotEye(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(32.dp)
                        .offset(x = 5.dp, y = 5.dp)
                )
                // Right Eye
                CarrotEye(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .offset(x = (-5).dp, y = 0.dp)
                )

                // Mouth
                Canvas(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-5).dp)
                ) {
                    val mouthPath = Path().apply {
                        moveTo(size.width * 0.1f, size.height * 0.2f)
                        cubicTo(
                            size.width * 0.2f, size.height * 0.9f,
                            size.width * 0.8f, size.height * 0.9f,
                            size.width * 0.9f, size.height * 0.2f
                        )
                        close()
                    }
                    drawPath(mouthPath, Color(0xFF4E342E))
                    
                    val innerMouthPath = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.5f)
                        quadraticTo(size.width * 0.5f, size.height * 0.85f, size.width * 0.8f, size.height * 0.5f)
                    }
                    drawPath(innerMouthPath, Color(0xFFE57373))
                }
            }
        }

        // Accents / Sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFFFD54F), radius = 8f, center = Offset(size.width * 0.85f, size.height * 0.2f))
            drawCircle(Color(0xFFFFD54F), radius = 5f, center = Offset(size.width * 0.92f, size.height * 0.35f))
            drawCircle(Color(0xFFFFD54F), radius = 4f, center = Offset(size.width * 0.94f, size.height * 0.48f))
            
            // Motion lines for seed
            val motionPath1 = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.35f)
                quadraticTo(size.width * 0.25f, size.height * 0.3f, size.width * 0.35f, size.height * 0.32f)
            }
            val motionPath2 = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.4f)
                quadraticTo(size.width * 0.3f, size.height * 0.37f, size.width * 0.4f, size.height * 0.38f)
            }
            drawPath(motionPath1, Color(0xFFFFB74D), style = Stroke(width = 3f))
            drawPath(motionPath2, Color(0xFFFFB74D), style = Stroke(width = 3f))

            // Floating seed/leaf
            val seedPath = Path().apply {
                val center = Offset(size.width * 0.25f, size.height * 0.45f)
                val width = 30f
                val height = 40f
                moveTo(center.x, center.y - height/2)
                quadraticTo(center.x + width/2, center.y, center.x, center.y + height/2)
                quadraticTo(center.x - width/2, center.y, center.x, center.y - height/2)
            }
            drawPath(seedPath, Color(0xFFFFCC80))
            drawPath(seedPath, Color(0xFF4E342E), style = Stroke(width = 3f))
        }
    }
}

private fun DrawScope.drawLeaf(center: Offset, radius: Float) {
    val leafPath = Path().apply {
        moveTo(center.x - radius * 0.8f, center.y)
        quadraticTo(center.x, center.y - radius * 0.6f, center.x + radius * 0.8f, center.y)
        quadraticTo(center.x, center.y + radius * 0.6f, center.x - radius * 0.8f, center.y)
    }
    drawPath(leafPath, Color(0xFF8BC34A))
    drawPath(leafPath, Color(0xFF33691E), style = Stroke(width = 3f))
}

private fun DrawScope.drawCarrotTop(size: Size) {
    val greenColor = Color(0xFF689F38)
    val darkGreenColor = Color(0xFF33691E)
    
    fun leaf(offset: Offset) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height)
            cubicTo(
                size.width * (0.5f + offset.x), size.height * (0.8f + offset.y),
                size.width * (0.5f + offset.x * 1.5f), size.height * (0.2f + offset.y),
                size.width * (0.5f + offset.x * 0.5f), size.height * (0.1f + offset.y)
            )
            cubicTo(
                size.width * (0.5f - offset.x * 0.5f), size.height * (0.1f + offset.y),
                size.width * (0.5f - offset.x * 1.5f), size.height * (0.2f + offset.y),
                size.width * 0.5f, size.height
            )
        }
        drawPath(path, greenColor)
        drawPath(path, darkGreenColor, style = Stroke(width = 3f))
    }
    
    leaf(Offset(0.1f, 0f))
    leaf(Offset(-0.15f, 0.1f))
    leaf(Offset(0.2f, 0.2f))
    leaf(Offset(-0.05f, -0.1f))
}

private fun DrawScope.drawCarrotLines(size: Size) {
    val lineColor = Color(0xFFE65100).copy(alpha = 0.5f)
    
    drawLine(lineColor, Offset(size.width * 0.3f, size.height * 0.3f), Offset(size.width * 0.5f, size.height * 0.32f), strokeWidth = 3f)
    drawLine(lineColor, Offset(size.width * 0.6f, size.height * 0.45f), Offset(size.width * 0.8f, size.height * 0.43f), strokeWidth = 3f)
    drawLine(lineColor, Offset(size.width * 0.4f, size.height * 0.65f), Offset(size.width * 0.6f, size.height * 0.67f), strokeWidth = 3f)
    drawLine(lineColor, Offset(size.width * 0.5f, size.height * 0.85f), Offset(size.width * 0.7f, size.height * 0.83f), strokeWidth = 3f)
}

@Composable
private fun CarrotEye(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 3f,
                center = Offset(size.width * 0.6f, size.height * 0.6f)
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 8f,
                center = Offset(size.width * 0.7f, size.height * 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CarrotIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CarrotIllustration()
        }
    }
}
