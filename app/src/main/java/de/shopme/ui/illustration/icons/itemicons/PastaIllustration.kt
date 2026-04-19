package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.BrandWhite
import de.shopme.ui.theme.BrandBlack

@Composable
fun PastaIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.pasta_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.85f, size.height * 0.15f)
            )
        }

        // Spaghetti
        SpaghettiCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = (-20).dp)
        )

        // Pasta Bag
        PastaBagCharacter(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 10.dp)
        )

        // Macaroni pieces
        MacaroniCharacter(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-20).dp)
                .rotate(-15f)
        )

        MacaroniCharacter(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.BottomCenter)
                .offset(x = (-40).dp, y = 10.dp)
                .rotate(10f)
        )

        MacaroniCharacter(
            modifier = Modifier
                .size(65.dp)
                .align(Alignment.BottomCenter)
                .offset(x = 50.dp, y = (-10).dp)
                .rotate(-40f)
        )
    }
}

@Composable
private fun SpaghettiCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spaghettiColor = Color(0xFFFFD54F)
            val strokeColor = Color(0xFF5D4037)
            val count = 12
            val spacing = size.width / (count + 1)

            for (i in 1..count) {
                val x = i * spacing
                drawLine(
                    color = spaghettiColor,
                    start = Offset(x, size.height * 0.05f),
                    end = Offset(x, size.height * 0.95f),
                    strokeWidth = 12f
                )
                drawLine(
                    color = strokeColor,
                    start = Offset(x, size.height * 0.05f),
                    end = Offset(x, size.height * 0.95f),
                    strokeWidth = 2f,
                    alpha = 0.3f
                )
                // Top circle
                drawCircle(
                    color = spaghettiColor,
                    radius = 6f,
                    center = Offset(x, size.height * 0.05f)
                )
                drawCircle(
                    color = strokeColor,
                    radius = 6f,
                    center = Offset(x, size.height * 0.05f),
                    style = Stroke(width = 2f)
                )
            }

            // Ribbon
            val ribbonY = size.height * 0.55f
            val ribbonPath = Path().apply {
                moveTo(size.width * 0.1f, ribbonY)
                lineTo(size.width * 0.9f, ribbonY)
                lineTo(size.width * 0.9f, ribbonY + 25f)
                lineTo(size.width * 0.1f, ribbonY + 25f)
                close()
            }
            drawPath(path = ribbonPath, color = Color(0xFFE53935))
            drawPath(path = ribbonPath, color = Color(0xFFB71C1C), style = Stroke(width = 4f))

            // Ribbon Bow
            val bowPath = Path().apply {
                moveTo(size.width * 0.15f, ribbonY + 12f)
                cubicTo(
                    -20f, ribbonY - 40f,
                    -20f, ribbonY + 80f,
                    size.width * 0.15f, ribbonY + 12f
                )
            }
            drawPath(path = bowPath, color = Color(0xFFE53935))
            drawPath(path = bowPath, color = Color(0xFFB71C1C), style = Stroke(width = 4f))
        }

        // Face
        Box(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.Center)
                .offset(y = (-20).dp)
        ) {
            Eye(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(24.dp)
            )
            Eye(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            )
            // Mouth
            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-5).dp)
            ) {
                val mouthPath = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(size.width / 2f, size.height, size.width, 0f)
                    close()
                }
                drawPath(path = mouthPath, color = Color(0xFF3E0000))
            }
        }
    }
}

@Composable
private fun PastaBagCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bagPath = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.1f)
                lineTo(size.width * 0.85f, size.height * 0.15f)
                lineTo(size.width * 0.95f, size.height * 0.85f)
                quadraticTo(size.width * 0.95f, size.height * 0.95f, size.width * 0.8f, size.height * 0.95f)
                lineTo(size.width * 0.15f, size.height * 0.9f)
                quadraticTo(size.width * 0.05f, size.height * 0.9f, size.width * 0.05f, size.height * 0.8f)
                close()
            }
            drawPath(
                path = bagPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFE53935)),
                    startY = 0f,
                    endY = size.height
                )
            )
            drawPath(path = bagPath, color = Color(0xFF5D4037), style = Stroke(width = 6f))

            // Transparent window
            val windowPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.45f)
                lineTo(size.width * 0.85f, size.height * 0.5f)
                lineTo(size.width * 0.88f, size.height * 0.8f)
                lineTo(size.width * 0.18f, size.height * 0.78f)
                close()
            }
            drawPath(path = windowPath, color = BrandWhite.copy(alpha = 0.4f))
            drawPath(path = windowPath, color = Color(0xFF5D4037), style = Stroke(width = 4f))

            // Penne inside window (simplified)
            for (i in 0..5) {
                val x = size.width * (0.25f + i * 0.1f)
                val y = size.height * (0.55f + (i % 2) * 0.05f)
                drawRoundRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(x, y),
                    size = Size(size.width * 0.06f, size.height * 0.15f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 3f)
                )
            }
        }

        // Header with "NUDELN"
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.TopCenter)
                .offset(y = 35.dp)
                .rotate(5f)
                .background(Color(0xFFE53935))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.nudeln_label),
                color = BrandWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Face on Bag
        Box(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.Center)
                .offset(y = (-20).dp, x = 10.dp)
        ) {
            Eye(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(36.dp)
            )
            Eye(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            )
            // Mouth
            Canvas(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp)
            ) {
                val mouthPath = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(size.width / 2f, size.height, size.width, 0f)
                    close()
                }
                drawPath(path = mouthPath, color = Color(0xFF3E0000))
                // Tongue
                drawCircle(
                    color = Color(0xFFFF5252),
                    radius = size.width / 4f,
                    center = Offset(size.width / 2f, size.height * 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MacaroniCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val macaroniColor = Color(0xFFFFB74D)
            val strokeColor = Color(0xFF5D4037)
            val path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.5f)
                cubicTo(
                    size.width * 0.1f, size.height * 0.1f,
                    size.width * 0.9f, size.height * 0.1f,
                    size.width * 0.9f, size.height * 0.5f
                )
                lineTo(size.width * 0.7f, size.height * 0.5f)
                cubicTo(
                    size.width * 0.7f, size.height * 0.3f,
                    size.width * 0.3f, size.height * 0.3f,
                    size.width * 0.3f, size.height * 0.5f
                )
                close()
            }
            drawPath(color = macaroniColor, path = path)
            drawPath(path = path, color = strokeColor, style = Stroke(width = 4f))

            // Eyes
            drawCircle(strokeColor, radius = 3f, center = Offset(size.width * 0.4f, size.height * 0.4f))
            drawCircle(strokeColor, radius = 3f, center = Offset(size.width * 0.6f, size.height * 0.4f))
            // Mouth
            drawArc(
                color = strokeColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(size.width * 0.45f, size.height * 0.42f),
                size = Size(size.width * 0.1f, size.height * 0.08f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun Eye(modifier: Modifier = Modifier) {
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
                radius = size.minDimension / 4f,
                center = center
            )
            drawCircle(
                color = BrandWhite,
                radius = size.minDimension / 10f,
                center = Offset(center.x + size.width / 10f, center.y - size.height / 10f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PastaIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PastaIllustration()
    }
}
