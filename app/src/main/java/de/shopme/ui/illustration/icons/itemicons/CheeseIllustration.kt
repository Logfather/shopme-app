package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun CheeseIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.cheese_illustration_description)
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
                .offset(y = (-15).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.15f)
            )
        }

        // Bottom Leaves (Small green touches)
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
                .offset(y = (-5).dp)
        ) {
            val leafPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.82f)
                quadraticTo(size.width * 0.05f, size.height * 0.75f, size.width * 0.25f, size.height * 0.75f)
                quadraticTo(size.width * 0.4f, size.height * 0.82f, size.width * 0.1f, size.height * 0.82f)
            }
            drawPath(leafPath, Color(0xFF8DB600))
            drawPath(leafPath, Color(0xFF2E4600), style = Stroke(width = 3f))
        }

        // Main Cheese Body
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .align(Alignment.Center)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Top surface
                val topSurface = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.35f)
                    lineTo(size.width * 0.75f, size.height * 0.15f)
                    lineTo(size.width * 0.95f, size.height * 0.3f)
                    lineTo(size.width * 0.45f, size.height * 0.45f)
                    close()
                }
                drawPath(topSurface, Color(0xFFFFEB3B))

                // Front surface
                val frontSurface = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.35f)
                    lineTo(size.width * 0.45f, size.height * 0.45f)
                    lineTo(size.width * 0.45f, size.height * 0.85f)
                    lineTo(size.width * 0.25f, size.height * 0.85f)
                    close()
                }
                drawPath(frontSurface, Color(0xFFFDD835))

                // Right surface (Side)
                val rightSurface = Path().apply {
                    moveTo(size.width * 0.45f, size.height * 0.45f)
                    lineTo(size.width * 0.95f, size.height * 0.3f)
                    lineTo(size.width * 0.85f, size.height * 0.8f)
                    lineTo(size.width * 0.45f, size.height * 0.85f)
                    close()
                }
                drawPath(rightSurface, Color(0xFFFED73F))

                // Holes
                drawCheeseHole(Offset(size.width * 0.6f, size.height * 0.25f), 15f)
                drawCheeseHole(Offset(size.width * 0.35f, size.height * 0.55f), 20f)
                drawCheeseHole(Offset(size.width * 0.75f, size.height * 0.5f), 25f)
                drawCheeseHole(Offset(size.width * 0.8f, size.height * 0.35f), 12f)
                drawCheeseHole(Offset(size.width * 0.35f, size.height * 0.75f), 15f)
                drawCheeseHole(Offset(size.width * 0.82f, size.height * 0.65f), 18f)

                // Outline
                val fullOutline = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.35f)
                    lineTo(size.width * 0.75f, size.height * 0.15f)
                    lineTo(size.width * 0.95f, size.height * 0.3f)
                    lineTo(size.width * 0.85f, size.height * 0.8f)
                    lineTo(size.width * 0.45f, size.height * 0.85f)
                    lineTo(size.width * 0.25f, size.height * 0.85f)
                    close()
                }
                drawPath(fullOutline, Color(0xFF8B4513), style = Stroke(width = 6f))

                drawLine(
                    color = Color(0xFF8B4513),
                    start = Offset(size.width * 0.45f, size.height * 0.45f),
                    end = Offset(size.width * 0.45f, size.height * 0.85f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color(0xFF8B4513),
                    start = Offset(size.width * 0.2f, size.height * 0.35f),
                    end = Offset(size.width * 0.45f, size.height * 0.45f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color(0xFF8B4513),
                    start = Offset(size.width * 0.45f, size.height * 0.45f),
                    end = Offset(size.width * 0.95f, size.height * 0.3f),
                    strokeWidth = 4f
                )
            }

            // Face
            Box(
                modifier = Modifier
                    .fillMaxSize(0.6f)
                    .align(Alignment.Center)
                    .offset(x = 10.dp, y = 15.dp)
            ) {
                // Eyes
                Row(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    CheeseEye(modifier = Modifier.size(40.dp))
                    CheeseEye(modifier = Modifier.size(40.dp))
                }

                // Mouth
                Canvas(
                    modifier = Modifier
                        .size(50.dp, 35.dp)
                        .align(Alignment.Center)
                        .offset(y = 30.dp)
                ) {
                    val mouthPath = Path().apply {
                        moveTo(0f, 0f)
                        cubicTo(0f, size.height, size.width, size.height, size.width, 0f)
                        close()
                    }
                    drawPath(mouthPath, Color(0xFF3E0000))

                    val tonguePath = Path().apply {
                        val rect = Rect(size.width * 0.2f, size.height * 0.6f, size.width * 0.8f, size.height * 1.1f)
                        addOval(rect)
                    }
                    drawPath(tonguePath, Color(0xFFFF5252))
                }
            }
        }

        // Small piece on the right
        Canvas(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = (-25).dp)
        ) {
            val piecePath = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.3f)
                lineTo(size.width * 0.8f, size.height * 0.4f)
                lineTo(size.width * 0.7f, size.height * 0.8f)
                lineTo(size.width * 0.1f, size.height * 0.7f)
                close()
            }
            drawPath(piecePath, Color(0xFFFED73F))
            drawPath(piecePath, Color(0xFF8B4513), style = Stroke(width = 4f))
        }

        // Crumbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFFED73F), radius = 6f, center = Offset(size.width * 0.2f, size.height * 0.9f))
            drawCircle(Color(0xFFFED73F), radius = 8f, center = Offset(size.width * 0.38f, size.height * 0.93f))
            drawCircle(Color(0xFFFED73F), radius = 5f, center = Offset(size.width * 0.95f, size.height * 0.88f))
        }
    }
}

@Composable
private fun CheeseEye(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(BrandWhite, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBlack,
                radius = size.minDimension / 3.5f,
                center = center
            )
            drawCircle(
                color = BrandWhite,
                radius = size.minDimension / 10f,
                center = Offset(center.x + 3f, center.y - 3f)
            )
        }
    }
}

private fun DrawScope.drawCheeseHole(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFFD48227),
        radius = radius,
        center = center
    )
}

@Preview(showBackground = true)
@Composable
private fun CheeseIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CheeseIllustration()
    }
}
