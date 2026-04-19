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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun SnacksIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.snacks_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.12f)
            )
        }

        // Chips Bag (Back center)
        ChipsBag(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.Center)
                .offset(y = (-30).dp)
        )

        // Soda Cup (Left)
        SodaCup(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .align(Alignment.CenterStart)
                .offset(x = 5.dp, y = 20.dp)
        )

        // Popcorn Box (Right)
        PopcornBox(
            modifier = Modifier
                .fillMaxSize(0.52f)
                .align(Alignment.CenterEnd)
                .offset(x = (-5.dp), y = 30.dp)
        )

        // Chocolate Bar (Front center)
        ChocolateBar(
            modifier = Modifier
                .fillMaxSize(0.42f)
                .align(Alignment.BottomCenter)
                .offset(x = (-25).dp, y = (-25).dp)
        )

        // Candies on the ground
        Candies(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SodaCup(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Straw
        val strawPath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.7f, h * (-0.15f))
        }
        drawPath(strawPath, BrandWhite, style = Stroke(width = 18f))
        // Straw stripes
        val stripeColor = Color(0xFFE53935)
        for (i in 0..4) {
            val progress = i / 4f
            val startX = w * 0.5f + (w * 0.2f * progress)
            val startY = h * 0.15f - (h * 0.3f * progress)
            drawCircle(stripeColor, radius = 9f, center = Offset(startX, startY))
        }

        // Cup body
        val cupPath = Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.65f, h * 0.95f)
            lineTo(w * 0.35f, h * 0.95f)
            close()
        }
        drawPath(cupPath, Color(0xFFE53935))
        drawPath(cupPath, Color(0xFFB71C1C), style = Stroke(width = 4f))

        // Lid
        drawRoundRect(
            color = BrandWhite,
            topLeft = Offset(w * 0.2f, h * 0.2f),
            size = Size(w * 0.6f, h * 0.08f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFB0BEC5),
            topLeft = Offset(w * 0.2f, h * 0.2f),
            size = Size(w * 0.6f, h * 0.08f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2f)
        )

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.55f), w * 0.35f)
    }
}

@Composable
private fun ChipsBag(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val chipsLabel = stringResource(R.string.chips_label)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bag body
        val bagPath = Path().apply {
            moveTo(w * 0.15f, h * 0.25f)
            quadraticTo(w * 0.5f, h * 0.2f, w * 0.85f, h * 0.25f)
            lineTo(w * 0.8f, h * 0.95f)
            quadraticTo(w * 0.5f, h * 1.05f, w * 0.2f, h * 0.95f)
            close()
        }
        drawPath(bagPath, Color(0xFFE53935))
        
        // Checkered pattern top
        for (i in 0..8) {
            for (j in 0..1) {
                if ((i + j) % 2 == 0) {
                    drawRect(
                        color = BrandWhite.copy(alpha = 0.3f),
                        topLeft = Offset(w * (0.18f + i * 0.08f), h * (0.24f + j * 0.04f)),
                        size = Size(w * 0.08f, h * 0.04f)
                    )
                }
            }
        }
        drawPath(bagPath, Color(0xFFB71C1C), style = Stroke(width = 4f))

        // Chips
        for (i in 0..5) {
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 35f,
                center = Offset(w * (0.35f + i * 0.08f), h * (0.12f + (i % 2) * 0.04f))
            )
            drawCircle(
                color = Color(0xFFF9A825),
                radius = 35f,
                center = Offset(w * (0.35f + i * 0.08f), h * (0.12f + (i % 2) * 0.04f)),
                style = Stroke(width = 2f)
            )
        }

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.45f), w * 0.4f)

        // Label
        drawText(
            textMeasurer = textMeasurer,
            text = chipsLabel,
            style = TextStyle(
                color = Color(0xFFFFEB3B),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            ),
            topLeft = Offset(w * 0.32f, h * 0.62f)
        )
    }
}

@Composable
private fun PopcornBox(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Box
        val boxPath = Path().apply {
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.35f)
            lineTo(w * 0.7f, h * 0.95f)
            lineTo(w * 0.3f, h * 0.95f)
            close()
        }
        drawPath(boxPath, BrandWhite)
        
        // Stripes
        for (i in 0..4) {
            drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(w * (0.28f + i * 0.12f), h * 0.35f),
                size = Size(w * 0.06f, h * 0.6f)
            )
        }
        drawPath(boxPath, Color(0xFFB71C1C), style = Stroke(width = 4f))

        // Popcorn
        for (i in 0..15) {
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 22f,
                center = Offset(w * (0.25f + i * 0.035f), h * (0.3f - (i % 4) * 0.03f))
            )
            drawCircle(
                color = Color(0xFFFBC02D).copy(alpha = 0.5f),
                radius = 22f,
                center = Offset(w * (0.25f + i * 0.035f), h * (0.3f - (i % 4) * 0.03f)),
                style = Stroke(width = 1f)
            )
        }

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.6f), w * 0.35f)
    }
}

@Composable
private fun ChocolateBar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Chocolate
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.25f, h * 0.3f),
            size = Size(w * 0.5f, h * 0.45f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Chocolate grid
        for (i in 1..2) {
            drawLine(Color(0xFF3E2723), Offset(w * (0.25f + i * 0.16f), h * 0.3f), Offset(w * (0.25f + i * 0.16f), h * 0.75f), strokeWidth = 2f)
        }
        drawLine(Color(0xFF3E2723), Offset(w * 0.25f, h * 0.52f), Offset(w * 0.75f, h * 0.52f), strokeWidth = 2f)

        // Wrapper
        val wrapperPath = Path().apply {
            moveTo(w * 0.2f, h * 0.68f)
            lineTo(w * 0.8f, h * 0.68f)
            lineTo(w * 0.75f, h * 0.95f)
            lineTo(w * 0.25f, h * 0.95f)
            close()
        }
        drawPath(wrapperPath, Color(0xFFE53935))
        drawPath(wrapperPath, Color(0xFFB71C1C), style = Stroke(width = 4f))

        // Silver foil
        val foilPath = Path().apply {
            moveTo(w * 0.2f, h * 0.68f)
            lineTo(w * 0.8f, h * 0.68f)
            quadraticTo(w * 0.5f, h * 0.6f, w * 0.2f, h * 0.68f)
        }
        drawPath(foilPath, Color(0xFFB0BEC5))

        // Face
        drawCuteFace(this, Offset(w * 0.5f, h * 0.52f), w * 0.3f)
    }
}

@Composable
private fun Candies(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val candyColors = listOf(Color(0xFFFFC107), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFFF5722), Color(0xFF9C27B0))
        for (i in 0..30) {
            val x = size.width * (0.05f + (i * 137.5f % 0.9f))
            val y = size.height * (0.85f + (i * 31.4f % 0.1f))
            drawCircle(
                color = candyColors[i % candyColors.size],
                radius = 12f,
                center = Offset(x, y)
            )
            drawCircle(
                color = BrandBlack.copy(alpha = 0.2f),
                radius = 12f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
    }
}

private fun drawCuteFace(drawScope: DrawScope, center: Offset, width: Float) {
    with(drawScope) {
        val eyeSize = width * 0.38f
        val eyeOffset = width * 0.28f
        
        // Eyes
        drawCircle(BrandWhite, radius = eyeSize / 2, center = Offset(center.x - eyeOffset, center.y - 12f))
        drawCircle(BrandBlack, radius = eyeSize / 5, center = Offset(center.x - eyeOffset + 4f, center.y - 10f))
        drawCircle(BrandBlack, radius = eyeSize / 2, center = Offset(center.x - eyeOffset, center.y - 12f), style = Stroke(width = 2f))
        
        drawCircle(BrandWhite, radius = eyeSize / 2, center = Offset(center.x + eyeOffset, center.y - 12f))
        drawCircle(BrandBlack, radius = eyeSize / 5, center = Offset(center.x + eyeOffset - 4f, center.y - 10f))
        drawCircle(BrandBlack, radius = eyeSize / 2, center = Offset(center.x + eyeOffset, center.y - 12f), style = Stroke(width = 2f))

        // Mouth (Happy wide smile)
        val mouthRect = Rect(center.x - width * 0.25f, center.y + 5f, center.x + width * 0.25f, center.y + width * 0.3f)
        val mouthPath = Path().apply {
            moveTo(mouthRect.left, mouthRect.top)
            quadraticTo(center.x, mouthRect.bottom + 15f, mouthRect.right, mouthRect.top)
            quadraticTo(center.x, mouthRect.top + 5f, mouthRect.left, mouthRect.top)
            close()
        }
        drawPath(mouthPath, Color(0xFF212121))
        
        // Tongue/Inner mouth
        val tonguePath = Path().apply {
            moveTo(center.x - width * 0.15f, mouthRect.bottom)
            quadraticTo(center.x, mouthRect.bottom + 10f, center.x + width * 0.15f, mouthRect.bottom)
            quadraticTo(center.x, mouthRect.bottom + 5f, center.x - width * 0.15f, mouthRect.bottom)
            close()
        }
        drawPath(tonguePath, Color(0xFFE53935))
    }
}

@Preview(showBackground = true)
@Composable
private fun SnacksIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        SnacksIllustration()
    }
}
