package de.shopme.ui.illustration.icons.syncicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
fun PendingIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.out_of_time_illustration_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val w = size.width
        val h = size.height

        val darkGoldColor = Color(0xFFFBC02D)
        val faceColor = Color(0xFFFFF9C4)
        val redColor = Color(0xFFE53935)
        val brownColor = Color(0xFF795548)
        val outlineColor = Color(0xFF3E2723)
        val sweatColor = Color(0xFF81D4FA)
        val sunColor = Color(0xFFFFE082)
        val sandColor = Color(0xFFFFEB3B)

        // Background Arc/Sun dots
        val centerX = w * 0.5f
        val centerY = h * 0.45f
        val radius = w * 0.4f
        for (i in 0 until 18) {
            val angle = 180f + (i * 10.5f)
            withTransform({
                rotate(degrees = angle, pivot = Offset(centerX, centerY))
            }) {
                drawRoundRect(
                    color = sunColor,
                    topLeft = Offset(centerX + radius, centerY - 15f),
                    size = Size(40f, 20f),
                    cornerRadius = CornerRadius(10f)
                )
            }
        }

        // Alarm Clock Body
        val clockCenter = Offset(w * 0.52f, h * 0.45f)
        val clockRadius = w * 0.28f

        // Shadow/Outer
        drawCircle(
            color = darkGoldColor,
            radius = clockRadius,
            center = clockCenter
        )
        drawCircle(
            color = outlineColor,
            radius = clockRadius,
            center = clockCenter,
            style = Stroke(width = 6f)
        )

        // Inner Face
        drawCircle(
            color = faceColor,
            radius = clockRadius * 0.85f,
            center = clockCenter
        )
        drawCircle(
            color = outlineColor,
            radius = clockRadius * 0.85f,
            center = clockCenter,
            style = Stroke(width = 4f)
        )

        // Bells
        fun drawBell(offset: Offset, rotation: Float) {
            withTransform({
                translate(offset.x, offset.y)
                rotate(rotation)
            }) {
                val bellPath = Path().apply {
                    arcTo(Rect(-40f, -40f, 40f, 40f), 180f, 180f, true)
                    lineTo(40f, 0f)
                    lineTo(-40f, 0f)
                    close()
                }
                drawPath(bellPath, redColor)
                drawPath(bellPath, outlineColor, style = Stroke(width = 4f, join = StrokeJoin.Round))
                
                // Bell highlight
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 10f, center = Offset(-15f, -15f))
            }
        }

        drawBell(Offset(w * 0.38f, h * 0.28f), -35f)
        drawBell(Offset(w * 0.65f, h * 0.28f), 35f)

        // Handle and Legs
        drawRoundRect(
            color = brownColor,
            topLeft = Offset(w * 0.48f, h * 0.2f),
            size = Size(w * 0.08f, 15f),
            cornerRadius = CornerRadius(5f)
        )
        drawRect(outlineColor, topLeft = Offset(w * 0.48f, h * 0.2f), size = Size(w * 0.08f, 15f), style = Stroke(width = 4f))

        // Legs
        val legW = 15f
        val legH = 40f
        withTransform({ rotate(-20f, Offset(w * 0.4f, h * 0.62f)) }) {
            drawRoundRect(brownColor, Offset(w * 0.4f, h * 0.62f), Size(legW, legH), CornerRadius(5f))
            drawRoundRect(outlineColor, Offset(w * 0.4f, h * 0.62f), Size(legW, legH), CornerRadius(5f), style = Stroke(width = 4f))
        }
        withTransform({ rotate(20f, Offset(w * 0.62f, h * 0.62f)) }) {
            drawRoundRect(brownColor, Offset(w * 0.62f, h * 0.62f), Size(legW, legH), CornerRadius(5f))
            drawRoundRect(outlineColor, Offset(w * 0.62f, h * 0.62f), Size(legW, legH), CornerRadius(5f), style = Stroke(width = 4f))
        }

        // Eyes
        val eyeW = w * 0.12f
        val eyeH = w * 0.15f
        val leftEyeCenter = Offset(clockCenter.x - eyeW * 0.6f, clockCenter.y - eyeH * 0.3f)
        val rightEyeCenter = Offset(clockCenter.x + eyeW * 0.6f, clockCenter.y - eyeH * 0.3f)

        drawOval(Color.White, topLeft = Offset(leftEyeCenter.x - eyeW/2, leftEyeCenter.y - eyeH/2), size = Size(eyeW, eyeH))
        drawOval(outlineColor, topLeft = Offset(leftEyeCenter.x - eyeW/2, leftEyeCenter.y - eyeH/2), size = Size(eyeW, eyeH), style = Stroke(width = 4f))
        drawCircle(outlineColor, radius = 10f, center = Offset(leftEyeCenter.x + 5f, leftEyeCenter.y))

        drawOval(Color.White, topLeft = Offset(rightEyeCenter.x - eyeW/2, rightEyeCenter.y - eyeH/2), size = Size(eyeW, eyeH))
        drawOval(outlineColor, topLeft = Offset(rightEyeCenter.x - eyeW/2, rightEyeCenter.y - eyeH/2), size = Size(eyeW, eyeH), style = Stroke(width = 4f))
        drawCircle(outlineColor, radius = 10f, center = Offset(rightEyeCenter.x - 5f, rightEyeCenter.y))

        // Eyebrows
        val browPathL = Path().apply {
            moveTo(leftEyeCenter.x - 30f, leftEyeCenter.y - eyeH * 0.7f)
            quadraticTo(leftEyeCenter.x, leftEyeCenter.y - eyeH * 0.9f, leftEyeCenter.x + 20f, leftEyeCenter.y - eyeH * 0.6f)
        }
        drawPath(browPathL, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

        val browPathR = Path().apply {
            moveTo(rightEyeCenter.x - 20f, rightEyeCenter.y - eyeH * 0.6f)
            quadraticTo(rightEyeCenter.x, rightEyeCenter.y - eyeH * 0.9f, rightEyeCenter.x + 30f, rightEyeCenter.y - eyeH * 0.7f)
        }
        drawPath(browPathR, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

        // Mouth
        val mouthPath = Path().apply {
            moveTo(clockCenter.x - 40f, clockCenter.y + 40f)
            quadraticTo(clockCenter.x, clockCenter.y + 30f, clockCenter.x + 40f, clockCenter.y + 40f)
            quadraticTo(clockCenter.x + 50f, clockCenter.y + 70f, clockCenter.x, clockCenter.y + 80f)
            quadraticTo(clockCenter.x - 50f, clockCenter.y + 70f, clockCenter.x - 40f, clockCenter.y + 40f)
            close()
        }
        drawPath(mouthPath, Color(0xFF5D4037))
        drawPath(mouthPath, outlineColor, style = Stroke(width = 4f))

        // Sweat
        val sweatPath = Path().apply {
            moveTo(clockCenter.x + clockRadius * 0.6f, clockCenter.y - 20f)
            quadraticTo(clockCenter.x + clockRadius * 0.75f, clockCenter.y, clockCenter.x + clockRadius * 0.6f, clockCenter.y + 20f)
            quadraticTo(clockCenter.x + clockRadius * 0.45f, clockCenter.y, clockCenter.x + clockRadius * 0.6f, clockCenter.y - 20f)
        }
        drawPath(sweatPath, sweatColor)
        drawPath(sweatPath, outlineColor, style = Stroke(width = 2f))

        // Hourglass
        withTransform({
            translate(w * 0.2f, h * 0.65f)
            rotate(-15f)
        }) {
            val hgW = w * 0.18f
            val hgH = h * 0.28f
            
            // Glass bulbs
            val glassPath = Path().apply {
                moveTo(0f, 0f)
                cubicTo(hgW * 0.1f, hgH * 0.3f, hgW * 0.4f, hgH * 0.45f, hgW * 0.5f, hgH * 0.5f)
                cubicTo(hgW * 0.6f, hgH * 0.45f, hgW * 0.9f, hgH * 0.3f, hgW, 0f)
                
                moveTo(0f, hgH)
                cubicTo(hgW * 0.1f, hgH * 0.7f, hgW * 0.4f, hgH * 0.55f, hgW * 0.5f, hgH * 0.5f)
                cubicTo(hgW * 0.6f, hgH * 0.55f, hgW * 0.9f, hgH * 0.7f, hgW, hgH)
            }
            drawPath(glassPath, Color.White.copy(alpha = 0.5f))
            drawPath(glassPath, outlineColor, style = Stroke(width = 4f))

            // Sand
            val sandPathTop = Path().apply {
                moveTo(hgW * 0.2f, hgH * 0.1f)
                lineTo(hgW * 0.8f, hgH * 0.1f)
                lineTo(hgW * 0.55f, hgH * 0.45f)
                lineTo(hgW * 0.45f, hgH * 0.45f)
                close()
            }
            drawPath(sandPathTop, sandColor)

            val sandPathBottom = Path().apply {
                moveTo(hgW * 0.5f, hgH * 0.5f)
                lineTo(hgW * 0.8f, hgH * 0.9f)
                lineTo(hgW * 0.2f, hgH * 0.9f)
                close()
            }
            drawPath(sandPathBottom, sandColor)

            // Wooden ends
            drawRoundRect(brownColor, Offset(-10f, -20f), Size(hgW + 20f, 20f), CornerRadius(5f))
            drawRoundRect(outlineColor, Offset(-10f, -20f), Size(hgW + 20f, 20f), CornerRadius(5f), style = Stroke(width = 4f))
            
            drawRoundRect(brownColor, Offset(-10f, hgH), Size(hgW + 20f, 20f), CornerRadius(5f))
            drawRoundRect(outlineColor, Offset(-10f, hgH), Size(hgW + 20f, 20f), CornerRadius(5f), style = Stroke(width = 4f))
        }

        // Sparkles
        fun drawSparkle(offset: Offset, size: Float) {
            val path = Path().apply {
                moveTo(offset.x, offset.y - size)
                lineTo(offset.x + size * 0.2f, offset.y - size * 0.2f)
                lineTo(offset.x + size, offset.y)
                lineTo(offset.x + size * 0.2f, offset.y + size * 0.2f)
                lineTo(offset.x, offset.y + size)
                lineTo(offset.x - size * 0.2f, offset.y + size * 0.2f)
                lineTo(offset.x - size, offset.y)
                lineTo(offset.x - size * 0.2f, offset.y - size * 0.2f)
                close()
            }
            drawPath(path, sandColor)
            drawPath(path, outlineColor, style = Stroke(width = 1f))
        }

        drawSparkle(Offset(w * 0.2f, h * 0.25f), 15f)
        drawSparkle(Offset(w * 0.85f, h * 0.35f), 12f)
        drawSparkle(Offset(w * 0.8f, h * 0.55f), 10f)
    }
}

@Preview(showBackground = true)
@Composable
fun OutOfTimeIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp)
        ) {
            PendingIllustration(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
