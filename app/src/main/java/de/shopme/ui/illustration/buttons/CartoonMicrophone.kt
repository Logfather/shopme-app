package de.shopme.ui.illustration.buttons

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun CartoonMicrophone(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // Colors
        val outlineColor = Color(0xFF2D2D2D)
        val bodyColor = Color(0xFFBCC6CC)
        val bodyDark = Color(0xFF8A959E)
        val meshColor = Color(0xFF707070)
        val meshHighlight = Color(0xFFAAAAAA)
        val silverBandColor = Color(0xFFD1D1D1)
        val starColor = Color(0xFFFFD740)
        val noteBlue = Color(0xFF42A5F5)
        val noteYellow = Color(0xFFFFCA28)
        val blushColor = Color(0xFFFF8A80).copy(alpha = 0.6f)
        val mouthInside = Color(0xFF4E1414)
        val tongueColor = Color(0xFFFF5252)

        // Draw shadow
        drawOval(
            color = BrandBlack.copy(alpha = 0.1f),
            topLeft = Offset(canvasWidth * 0.25f, canvasHeight * 0.85f),
            size = Size(canvasWidth * 0.5f, canvasHeight * 0.1f)
        )

        // Rotation for the microphone body
        val rotation = -15f
        val pivot = Offset(centerX, centerY + canvasHeight * 0.2f)

        withTransform({
            rotate(rotation, pivot)
        }) {
            // Base
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(centerX - canvasWidth * 0.18f, canvasHeight * 0.81f),
                size = Size(canvasWidth * 0.36f, canvasHeight * 0.08f),
                cornerRadius = CornerRadius(canvasHeight * 0.04f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(BrandWhite, silverBandColor)),
                topLeft = Offset(centerX - canvasWidth * 0.17f, canvasHeight * 0.82f),
                size = Size(canvasWidth * 0.34f, canvasHeight * 0.06f),
                cornerRadius = CornerRadius(canvasHeight * 0.03f)
            )

            // Stem
            drawRect(
                color = outlineColor,
                topLeft = Offset(centerX - canvasWidth * 0.06f, canvasHeight * 0.73f),
                size = Size(canvasWidth * 0.12f, canvasHeight * 0.1f)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(bodyDark, bodyColor, bodyDark)),
                topLeft = Offset(centerX - canvasWidth * 0.05f, canvasHeight * 0.73f),
                size = Size(canvasWidth * 0.1f, canvasHeight * 0.1f)
            )

            // Microphone Body (Tapered)
            val bodyPath = Path().apply {
                moveTo(centerX - canvasWidth * 0.18f, canvasHeight * 0.35f)
                lineTo(centerX + canvasWidth * 0.18f, canvasHeight * 0.35f)
                lineTo(centerX + canvasWidth * 0.12f, canvasHeight * 0.75f)
                lineTo(centerX - canvasWidth * 0.12f, canvasHeight * 0.75f)
                close()
            }
            drawPath(bodyPath, outlineColor, style = Stroke(width = 4f))
            drawPath(
                path = bodyPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(bodyDark, BrandWhite, bodyDark),
                    startX = centerX - canvasWidth * 0.18f,
                    endX = centerX + canvasWidth * 0.18f
                )
            )

            // Bottom Cap
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(centerX - canvasWidth * 0.13f, canvasHeight * 0.74f),
                size = Size(canvasWidth * 0.26f, canvasHeight * 0.06f),
                cornerRadius = CornerRadius(10f)
            )

            // Silver Band
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(centerX - canvasWidth * 0.21f, canvasHeight * 0.32f),
                size = Size(canvasWidth * 0.42f, canvasHeight * 0.08f),
                cornerRadius = CornerRadius(canvasHeight * 0.04f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(BrandWhite, silverBandColor)),
                topLeft = Offset(centerX - canvasWidth * 0.2f, canvasHeight * 0.33f),
                size = Size(canvasWidth * 0.4f, canvasHeight * 0.06f),
                cornerRadius = CornerRadius(canvasHeight * 0.03f)
            )

            // Mesh Top
            val meshRect = Rect(centerX - canvasWidth * 0.22f, canvasHeight * 0.05f, centerX + canvasWidth * 0.22f, canvasHeight * 0.35f)
            val meshPath = Path().apply {
                addOval(meshRect)
            }
            
            drawPath(meshPath, outlineColor, style = Stroke(width = 4f))
            drawPath(
                path = meshPath,
                brush = Brush.radialGradient(
                    colors = listOf(meshHighlight, meshColor),
                    center = Offset(centerX, canvasHeight * 0.15f),
                    radius = canvasWidth * 0.25f
                )
            )

            // Mesh Grid
            clipPath(meshPath) {
                val step = canvasWidth * 0.04f
                for (i in -10..10) {
                    drawLine(
                        color = BrandBlack.copy(alpha = 0.3f),
                        start = Offset(centerX + i * step - 100, 0f),
                        end = Offset(centerX + i * step + 100, canvasHeight),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = BrandBlack.copy(alpha = 0.3f),
                        start = Offset(centerX + i * step + 100, 0f),
                        end = Offset(centerX + i * step - 100, canvasHeight),
                        strokeWidth = 1f
                    )
                }
            }

            // Face
            // Eyes
            val eyeRadiusX = canvasWidth * 0.07f
            val eyeRadiusY = canvasWidth * 0.08f
            
            // Left Eye
            drawOval(
                color = outlineColor,
                topLeft = Offset(centerX - canvasWidth * 0.15f - 2, canvasHeight * 0.45f - 2),
                size = Size(eyeRadiusX * 2 + 4, eyeRadiusY * 2 + 4)
            )
            drawOval(
                color = BrandWhite,
                topLeft = Offset(centerX - canvasWidth * 0.15f, canvasHeight * 0.45f),
                size = Size(eyeRadiusX * 2, eyeRadiusY * 2)
            )
            drawCircle(
                color = outlineColor,
                radius = eyeRadiusX * 0.5f,
                center = Offset(centerX - canvasWidth * 0.08f, canvasHeight * 0.55f)
            )
            drawCircle(
                color = BrandWhite,
                radius = eyeRadiusX * 0.15f,
                center = Offset(centerX - canvasWidth * 0.09f, canvasHeight * 0.53f)
            )

            // Right Eye
            drawOval(
                color = outlineColor,
                topLeft = Offset(centerX + canvasWidth * 0.01f - 2, canvasHeight * 0.46f - 2),
                size = Size(eyeRadiusX * 2 + 4, eyeRadiusY * 2 + 4)
            )
            drawOval(
                color = BrandWhite,
                topLeft = Offset(centerX + canvasWidth * 0.01f, canvasHeight * 0.46f),
                size = Size(eyeRadiusX * 2, eyeRadiusY * 2)
            )
            drawCircle(
                color = outlineColor,
                radius = eyeRadiusX * 0.5f,
                center = Offset(centerX + canvasWidth * 0.08f, canvasHeight * 0.56f)
            )
            drawCircle(
                color = BrandWhite,
                radius = eyeRadiusX * 0.15f,
                center = Offset(centerX + canvasWidth * 0.07f, canvasHeight * 0.54f)
            )

            // Blush
            drawCircle(
                color = blushColor,
                radius = canvasWidth * 0.04f,
                center = Offset(centerX - canvasWidth * 0.14f, canvasHeight * 0.62f)
            )
            drawCircle(
                color = blushColor,
                radius = canvasWidth * 0.04f,
                center = Offset(centerX + canvasWidth * 0.14f, canvasHeight * 0.64f)
            )

            // Mouth
            val mouthPath = Path().apply {
                moveTo(centerX - canvasWidth * 0.08f, canvasHeight * 0.65f)
                quadraticTo(centerX, canvasHeight * 0.68f, centerX + canvasWidth * 0.12f, canvasHeight * 0.67f)
                quadraticTo(centerX + canvasWidth * 0.08f, canvasHeight * 0.78f, centerX - canvasWidth * 0.02f, canvasHeight * 0.76f)
                close()
            }
            drawPath(mouthPath, outlineColor, style = Stroke(width = 3f))
            drawPath(mouthPath, mouthInside)

            // Tongue
            clipPath(mouthPath) {
                drawCircle(
                    color = tongueColor,
                    radius = canvasWidth * 0.05f,
                    center = Offset(centerX + canvasWidth * 0.05f, canvasHeight * 0.76f)
                )
            }
        }

        // Stars
        fun drawStar(offset: Offset, scale: Float) {
            val starPath = Path().apply {
                val size = canvasWidth * 0.06f * scale
                moveTo(offset.x, offset.y - size)
                quadraticTo(offset.x, offset.y, offset.x + size, offset.y)
                quadraticTo(offset.x, offset.y, offset.x, offset.y + size)
                quadraticTo(offset.x, offset.y, offset.x - size, offset.y)
                quadraticTo(offset.x, offset.y, offset.x, offset.y - size)
                close()
            }
            drawPath(starPath, starColor)
            drawPath(starPath, outlineColor, style = Stroke(width = 1f))
        }

        drawStar(Offset(canvasWidth * 0.2f, canvasHeight * 0.35f), 0.6f)
        drawStar(Offset(canvasWidth * 0.3f, canvasHeight * 0.12f), 0.8f)
        drawStar(Offset(canvasWidth * 0.85f, canvasHeight * 0.25f), 1.0f)
        drawStar(Offset(canvasWidth * 0.82f, canvasHeight * 0.38f), 1.2f)
        drawStar(Offset(canvasWidth * 0.75f, canvasHeight * 0.48f), 0.9f)
        drawStar(Offset(canvasWidth * 0.25f, canvasHeight * 0.58f), 0.7f)

        // Musical Notes
        fun drawNote(offset: Offset, color: Color, rotation: Float) {
            withTransform({
                rotate(rotation, offset)
            }) {
                val headRadius = canvasWidth * 0.025f
                val stemHeight = canvasWidth * 0.08f
                val flagWidth = canvasWidth * 0.05f

                // Note head
                drawCircle(color = color, radius = headRadius, center = offset)
                drawCircle(color = outlineColor, radius = headRadius, center = offset, style = Stroke(width = 2f))

                // Stem
                drawLine(
                    color = outlineColor,
                    start = Offset(offset.x + headRadius, offset.y),
                    end = Offset(offset.x + headRadius, offset.y - stemHeight),
                    strokeWidth = 3f
                )

                // Flag
                val flagPath = Path().apply {
                    moveTo(offset.x + headRadius, offset.y - stemHeight)
                    quadraticTo(
                        offset.x + headRadius + flagWidth,
                        offset.y - stemHeight + flagWidth / 2,
                        offset.x + headRadius + flagWidth / 2,
                        offset.y - stemHeight + flagWidth
                    )
                }
                drawPath(flagPath, outlineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
        }

        drawNote(Offset(canvasWidth * 0.25f, canvasHeight * 0.22f), noteYellow, -10f)
        drawNote(Offset(canvasWidth * 0.35f, canvasHeight * 0.28f), noteBlue, 15f)
    }
}

@Preview(showBackground = true)
@Composable
private fun CartoonMicrophonePreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(24.dp)
    ) {
        CartoonMicrophone(
            modifier = Modifier.fillMaxSize()
        )
    }
}
