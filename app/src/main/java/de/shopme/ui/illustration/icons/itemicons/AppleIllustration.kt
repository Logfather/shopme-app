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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun TomatoIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.tomato_illustration_description)
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
                .offset(y = (-10).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.15f)
            )
        }

        // Bottom Leaves
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Left bottom leaf
                val leftLeafPath = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.85f)
                    quadraticTo(
                        size.width * 0.05f, size.height * 0.75f,
                        size.width * 0.25f, size.height * 0.7f
                    )
                    quadraticTo(
                        size.width * 0.35f, size.height * 0.8f,
                        size.width * 0.15f, size.height * 0.85f
                    )
                }
                drawPath(leftLeafPath, Color(0xFF8DB600))
                drawPath(leftLeafPath, Color(0xFF2E4600), style = Stroke(width = 4f))

                // Right bottom leaf
                val rightLeafPath = Path().apply {
                    moveTo(size.width * 0.85f, size.height * 0.85f)
                    quadraticTo(
                        size.width * 0.95f, size.height * 0.75f,
                        size.width * 0.75f, size.height * 0.7f
                    )
                    quadraticTo(
                        size.width * 0.65f, size.height * 0.8f,
                        size.width * 0.85f, size.height * 0.85f
                    )
                }
                drawPath(rightLeafPath, Color(0xFF8DB600))
                drawPath(rightLeafPath, Color(0xFF2E4600), style = Stroke(width = 4f))
            }
        }

        // Top Leaves / Stem
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
        ) {
            val stemPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.8f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.4f,
                    size.width * 0.4f, size.height * 0.1f,
                    size.width * 0.6f, size.height * 0.1f
                )
                cubicTo(
                    size.width * 0.8f, size.height * 0.1f,
                    size.width * 0.7f, size.height * 0.5f,
                    size.width * 0.5f, size.height * 0.8f
                )
            }
            drawPath(stemPath, Color(0xFF8DB600))
            drawPath(stemPath, Color(0xFF2E4600), style = Stroke(width = 4f))

            val stemPath2 = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.8f)
                cubicTo(
                    size.width * 0.7f, size.height * 0.4f,
                    size.width * 0.6f, size.height * 0.2f,
                    size.width * 0.4f, size.height * 0.2f
                )
                cubicTo(
                    size.width * 0.2f, size.height * 0.2f,
                    size.width * 0.3f, size.height * 0.6f,
                    size.width * 0.5f, size.height * 0.8f
                )
            }
            drawPath(stemPath2, Color(0xFFA4C639))
            drawPath(stemPath2, Color(0xFF2E4600), style = Stroke(width = 4f))
        }

        // Tomato Body
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .align(Alignment.Center)
                .offset(y = 10.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F)),
                        center = Offset.Unspecified,
                        radius = Float.POSITIVE_INFINITY
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Outline
                drawCircle(
                    color = Color(0xFF3E0000),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 8f)
                )

                // Highlights
                drawOval(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                    size = Size(size.width * 0.2f, size.height * 0.15f)
                )
            }

            // Eyes
            Box(
                modifier = Modifier
                    .fillMaxSize(0.6f)
                    .align(Alignment.Center)
                    .offset(y = (-5).dp)
            ) {
                // Left Eye
                Eye(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .offset(x = 10.dp)
                )
                // Right Eye
                Eye(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(45.dp)
                        .offset(x = (-5).dp)
                )
            }

            // Mouth
            Canvas(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .offset(y = 30.dp, x = 5.dp)
            ) {
                val mouthPath = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(size.width / 2f, size.height, size.width, 0f)
                }
                drawPath(
                    path = mouthPath,
                    color = Color(0xFF3E0000),
                    style = Stroke(width = 4f)
                )
            }
        }

        // Sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSparkle(Offset(size.width * 0.85f, size.height * 0.25f), 15f)
            drawCircle(Color(0xFFFDD835), radius = 6f, center = Offset(size.width * 0.92f, size.height * 0.35f))
            drawCircle(Color(0xFFFDD835), radius = 4f, center = Offset(size.width * 0.94f, size.height * 0.45f))
        }
    }
}

@Composable
private fun Eye(modifier: Modifier = Modifier) {
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
                radius = size.minDimension / 4f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 12f,
                center = Offset(center.x + 4f, center.y - 4f)
            )
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
    drawPath(path, Color(0xFFFDD835))
}

@Preview(showBackground = true)
@Composable
private fun TomatoIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TomatoIllustration()
        }
    }
}
