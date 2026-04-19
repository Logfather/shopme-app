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
import androidx.compose.ui.draw.rotate
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
import de.shopme.ui.theme.BrandWhite

@Composable
fun SodaIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.soda_illustration_description)
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
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.12f)
            )
        }

        // Soda Bottle
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .rotate(-5f)
                .offset(x = (-10).dp, y = (-15).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bottlePath = Path().apply {
                    // Neck top
                    moveTo(size.width * 0.45f, size.height * 0.08f)
                    lineTo(size.width * 0.65f, size.height * 0.08f)
                    // Neck
                    quadraticTo(size.width * 0.64f, size.height * 0.18f, size.width * 0.62f, size.height * 0.28f)
                    // Shoulder
                    quadraticTo(size.width * 0.78f, size.height * 0.38f, size.width * 0.80f, size.height * 0.58f)
                    // Body
                    lineTo(size.width * 0.84f, size.height * 0.88f)
                    // Bottom
                    quadraticTo(size.width * 0.84f, size.height * 0.95f, size.width * 0.6f, size.height * 0.95f)
                    quadraticTo(size.width * 0.32f, size.height * 0.95f, size.width * 0.32f, size.height * 0.88f)
                    // Left side
                    lineTo(size.width * 0.36f, size.height * 0.58f)
                    // Left shoulder
                    quadraticTo(size.width * 0.38f, size.height * 0.38f, size.width * 0.52f, size.height * 0.28f)
                    // Left neck
                    quadraticTo(size.width * 0.48f, size.height * 0.18f, size.width * 0.45f, size.height * 0.08f)
                }

                // Bottle Body (Soda Liquid)
                drawPath(
                    path = bottlePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF5D4037), Color(0xFF21110E)),
                        startY = 0f,
                        endY = size.height
                    )
                )
                
                // Bottle Outline
                drawPath(
                    path = bottlePath,
                    color = Color(0xFF3E2723),
                    style = Stroke(width = 8f)
                )

                // Label
                val labelPath = Path().apply {
                    moveTo(size.width * 0.37f, size.height * 0.48f)
                    lineTo(size.width * 0.80f, size.height * 0.48f)
                    lineTo(size.width * 0.83f, size.height * 0.68f)
                    lineTo(size.width * 0.34f, size.height * 0.68f)
                    close()
                }
                drawPath(labelPath, Color(0xFFFF5722))
                drawPath(labelPath, Color(0xFF3E2723), style = Stroke(width = 4f))

                // Cap
                val capPath = Path().apply {
                    moveTo(size.width * 0.43f, size.height * 0.05f)
                    lineTo(size.width * 0.67f, size.height * 0.05f)
                    quadraticTo(size.width * 0.69f, size.height * 0.12f, size.width * 0.65f, size.height * 0.14f)
                    lineTo(size.width * 0.45f, size.height * 0.14f)
                    quadraticTo(size.width * 0.41f, size.height * 0.12f, size.width * 0.43f, size.height * 0.05f)
                }
                drawPath(capPath, Color(0xFFB0BEC5))
                drawPath(capPath, Color(0xFF455A64), style = Stroke(width = 4f))
            }

            // Face on the label
            Box(
                modifier = Modifier
                    .fillMaxSize(0.42f)
                    .offset(y = 52.dp, x = 16.dp)
            ) {
                // Cheeks
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 14f,
                        center = Offset(size.width * 0.15f, size.height * 0.5f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 14f,
                        center = Offset(size.width * 0.85f, size.height * 0.5f)
                    )
                }

                // Eyes
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 22.dp, y = 8.dp)
                        .background(Color(0xFF21110E), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-22).dp, y = 8.dp)
                        .background(Color(0xFF21110E), CircleShape)
                )
                
                // Mouth
                Canvas(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .offset(y = 12.dp)
                ) {
                    val mouthPath = Path().apply {
                        moveTo(0f, 0f)
                        quadraticTo(size.width / 2f, size.height * 0.7f, size.width, 0f)
                    }
                    drawPath(
                        path = mouthPath,
                        color = Color(0xFF21110E),
                        style = Stroke(width = 5f)
                    )
                }
            }
        }

        // Grapes/Beans at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Grape(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp)
                    .offset(x = (-55).dp, y = (-18).dp)
                    .rotate(10f)
            )
            Grape(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(52.dp)
                    .offset(x = (-25).dp, y = (-22).dp)
                    .rotate(-5f)
            )
            Grape(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(42.dp)
                    .offset(x = (-65).dp, y = (-48).dp)
                    .rotate(-40f)
            )
        }

        // Bubbles and Sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Sparkle top right
            drawSparkle(Offset(size.width * 0.72f, size.height * 0.28f), 20f)
            
            // Bubbles
            drawCircle(Color(0xFFFFD54F), radius = 18f, center = Offset(size.width * 0.82f, size.height * 0.42f))
            drawCircle(Color(0xFFFFD54F), radius = 10f, center = Offset(size.width * 0.78f, size.height * 0.58f))
            drawCircle(Color(0xFFFFD54F), radius = 8f, center = Offset(size.width * 0.76f, size.height * 0.65f))
            
            drawCircle(Color(0xFFFFD54F), radius = 9f, center = Offset(size.width * 0.35f, size.height * 0.22f))
            drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(size.width * 0.38f, size.height * 0.16f))
        }
    }
}

@Composable
private fun Grape(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF8BC34A), CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF33691E),
                radius = size.minDimension / 2f,
                style = Stroke(width = 4f)
            )
            // Shine
            drawOval(
                color = BrandWhite.copy(alpha = 0.4f),
                topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                size = Size(size.width * 0.4f, size.height * 0.3f)
            )
            // Detail line
            val detailPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.7f)
                quadraticTo(size.width * 0.7f, size.height * 0.8f, size.width * 0.8f, size.height * 0.6f)
            }
            drawPath(detailPath, Color(0xFF33691E), style = Stroke(width = 2f))
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path, Color(0xFFFFD54F))
    drawPath(path, Color(0xFF3E2723), style = Stroke(width = 2f))
}

@Preview(showBackground = true)
@Composable
private fun SodaIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        SodaIllustration()
    }
}
