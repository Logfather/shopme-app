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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun MeatIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.meat_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        BaconCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 40.dp)
                .rotate(10f)
        )

        SteakCharacter(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 30.dp)
        )

        SausageCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-20).dp)
                .rotate(-15f)
        )

        Garnish(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-10).dp)
        )
    }
}

@Composable
private fun SteakCharacter(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.2f)
            cubicTo(
                size.width * 0.5f, size.height * 0.05f,
                size.width * 0.95f, size.height * 0.15f,
                size.width * 0.9f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.85f, size.height * 0.85f,
                size.width * 0.5f, size.height * 0.98f,
                size.width * 0.2f, size.height * 0.85f
            )
            cubicTo(
                size.width * 0.05f, size.height * 0.65f,
                size.width * 0.05f, size.height * 0.35f,
                size.width * 0.2f, size.height * 0.2f
            )
            close()
        }

        drawPath(path, Color(0xFF8B0000), style = Stroke(width = 15f))
        
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F)),
                center = Offset(size.width * 0.4f, size.height * 0.4f),
                radius = size.width * 0.8f
            )
        )

        val marblingPath = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.15f)
            quadraticTo(size.width * 0.5f, size.height * 0.25f, size.width * 0.35f, size.height * 0.4f)
            
            moveTo(size.width * 0.7f, size.height * 0.2f)
            quadraticTo(size.width * 0.8f, size.height * 0.4f, size.width * 0.65f, size.height * 0.6f)
            
            moveTo(size.width * 0.2f, size.height * 0.5f)
            quadraticTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.15f, size.height * 0.8f)
        }
        drawPath(marblingPath, BrandWhite.copy(alpha = 0.4f), style = Stroke(width = 8f))

        drawPath(path, BrandBlack, style = Stroke(width = 4f))

        val eyeRadius = size.width * 0.12f
        drawEye(Offset(size.width * 0.4f, size.height * 0.5f), eyeRadius)
        drawEye(Offset(size.width * 0.65f, size.height * 0.5f), eyeRadius)

        val mouthPath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.65f)
            quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.75f, size.height * 0.65f)
            quadraticTo(size.width * 0.55f, size.height * 0.6f, size.width * 0.35f, size.height * 0.65f)
        }
        drawPath(mouthPath, Color(0xFF4A0000))
        drawPath(mouthPath, BrandBlack, style = Stroke(width = 4f))
        
        val tonguePath = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.78f)
            quadraticTo(size.width * 0.55f, size.height * 0.88f, size.width * 0.65f, size.height * 0.78f)
        }
        drawPath(tonguePath, Color(0xFFFF8A80))
    }
}

@Composable
private fun BaconCharacter(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.1f)
            quadraticTo(size.width * 0.3f, size.height * 0.05f, size.width * 0.5f, size.height * 0.2f)
            quadraticTo(size.width * 0.7f, size.height * 0.35f, size.width * 0.9f, size.height * 0.25f)
            lineTo(size.width * 0.95f, size.height * 0.8f)
            quadraticTo(size.width * 0.7f, size.height * 0.9f, size.width * 0.5f, size.height * 0.75f)
            quadraticTo(size.width * 0.3f, size.height * 0.6f, size.width * 0.1f, size.height * 0.7f)
            close()
        }

        drawPath(path, Color(0xFFFFCDD2))
        
        val meatStripes = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.15f)
            quadraticTo(size.width * 0.4f, size.height * 0.1f, size.width * 0.6f, size.height * 0.25f)
            quadraticTo(size.width * 0.8f, size.height * 0.4f, size.width * 0.9f, size.height * 0.35f)
            lineTo(size.width * 0.92f, size.height * 0.55f)
            quadraticTo(size.width * 0.7f, size.height * 0.65f, size.width * 0.5f, size.height * 0.45f)
            quadraticTo(size.width * 0.3f, size.height * 0.3f, size.width * 0.12f, size.height * 0.4f)
            close()

            moveTo(size.width * 0.1f, size.height * 0.55f)
            quadraticTo(size.width * 0.3f, size.height * 0.45f, size.width * 0.5f, size.height * 0.65f)
            quadraticTo(size.width * 0.75f, size.height * 0.8f, size.width * 0.93f, size.height * 0.75f)
            lineTo(size.width * 0.94f, size.height * 0.85f)
            quadraticTo(size.width * 0.7f, size.height * 0.95f, size.width * 0.5f, size.height * 0.8f)
            quadraticTo(size.width * 0.3f, size.height * 0.65f, size.width * 0.08f, size.height * 0.75f)
            close()
        }
        drawPath(meatStripes, Color(0xFFC62828))

        drawPath(path, BrandBlack, style = Stroke(width = 4f))

        drawEye(Offset(size.width * 0.45f, size.height * 0.35f), size.width * 0.08f)
        drawEye(Offset(size.width * 0.65f, size.height * 0.4f), size.width * 0.08f)
        
        val mouthPath = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.5f)
            quadraticTo(size.width * 0.6f, size.height * 0.65f, size.width * 0.8f, size.height * 0.55f)
        }
        drawPath(mouthPath, BrandBlack, style = Stroke(width = 4f))
    }
}

@Composable
private fun SausageCharacter(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.5f)
            cubicTo(
                size.width * 0.2f, size.height * 0.1f,
                size.width * 0.8f, size.height * 0.1f,
                size.width * 0.85f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.8f, size.height * 0.9f,
                size.width * 0.2f, size.height * 0.9f,
                size.width * 0.15f, size.height * 0.5f
            )
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE64A19), Color(0xFFBF360C)),
                center = center,
                radius = size.width * 0.6f
            )
        )
        
        for (i in 0..15) {
            val x = (0.2f + (i * 0.04f)) * size.width
            val y = (0.3f + ((i % 5) * 0.1f)) * size.height
            drawCircle(Color(0xFFFFCCBC).copy(alpha = 0.3f), radius = 3f, center = Offset(x, y))
        }

        drawTiedEnd(Offset(size.width * 0.1f, size.height * 0.55f), rotation = 150f)
        drawTiedEnd(Offset(size.width * 0.9f, size.height * 0.45f), rotation = -30f)

        drawPath(path, BrandBlack, style = Stroke(width = 4f))

        drawEye(Offset(size.width * 0.45f, size.height * 0.45f), size.width * 0.1f)
        drawEye(Offset(size.width * 0.68f, size.height * 0.45f), size.width * 0.1f)
        
        drawLine(BrandBlack, Offset(size.width * 0.38f, size.height * 0.35f), Offset(size.width * 0.48f, size.height * 0.32f), strokeWidth = 4f)
        drawLine(BrandBlack, Offset(size.width * 0.65f, size.height * 0.32f), Offset(size.width * 0.75f, size.height * 0.35f), strokeWidth = 4f)

        val mouthPath = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.65f)
            quadraticTo(size.width * 0.5f, size.height * 0.6f, size.width * 0.55f, size.height * 0.65f)
            quadraticTo(size.width * 0.6f, size.height * 0.7f, size.width * 0.65f, size.height * 0.65f)
        }
        drawPath(mouthPath, BrandBlack, style = Stroke(width = 4f))
    }
}

@Composable
private fun Garnish(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val parsleyPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.7f)
            cubicTo(size.width * 0.05f, size.height * 0.6f, size.width * 0.05f, size.height * 0.4f, size.width * 0.2f, size.height * 0.5f)
            cubicTo(size.width * 0.2f, size.height * 0.3f, size.width * 0.4f, size.height * 0.3f, size.width * 0.4f, size.height * 0.5f)
            cubicTo(size.width * 0.55f, size.height * 0.4f, size.width * 0.55f, size.height * 0.6f, size.width * 0.4f, size.height * 0.7f)
            close()
        }
        drawPath(parsleyPath, Color(0xFF4CAF50))
        drawPath(parsleyPath, BrandBlack, style = Stroke(width = 3f))
        
        drawCircle(BrandBlack, radius = 8f, center = Offset(size.width * 0.6f, size.height * 0.75f))
        drawCircle(Color(0xFF43A047), radius = 7f, center = Offset(size.width * 0.5f, size.height * 0.85f))
        drawCircle(Color(0xFF5D4037), radius = 9f, center = Offset(size.width * 0.7f, size.height * 0.8f))
        
        for (i in 0..10) {
            drawCircle(Color(0xFFEEEEEE), radius = 2f, center = Offset(size.width * 0.7f + (i * 8f), size.height * 0.9f))
            drawCircle(BrandWhite, radius = 2.5f, center = Offset(size.width * 0.65f + (i * 6f), size.height * 0.95f))
        }
    }
}

private fun DrawScope.drawEye(center: Offset, radius: Float) {
    drawCircle(BrandWhite, radius = radius, center = center)
    drawCircle(BrandBlack, radius = radius, center = center, style = Stroke(width = 3f))
    drawCircle(BrandBlack, radius = radius * 0.5f, center = center.plus(Offset(radius * 0.2f, 0f)))
    drawCircle(BrandWhite, radius = radius * 0.15f, center = center.plus(Offset(radius * 0.3f, -radius * 0.2f)))
}

private fun DrawScope.drawTiedEnd(center: Offset, rotation: Float) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y)
            lineTo(center.x - 15f, center.y - 15f)
            lineTo(center.x - 15f, center.y + 15f)
            close()
        }
        drawPath(path, Color(0xFFBF360C))
        drawPath(path, BrandBlack, style = Stroke(width = 3f))
        
        drawCircle(Color(0xFFBF360C), radius = 5f, center = center)
        drawCircle(BrandBlack, radius = 5f, center = center, style = Stroke(width = 3f))
    }
}

@Preview(showBackground = true)
@Composable
private fun MeatIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        MeatIllustration()
    }
}
