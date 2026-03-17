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
fun FailedIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.sync_error_illustration_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val laptopBody = Color(0xFF2B3D4F)
        val screenBlue = Color(0xFF80D8FF)
        val warningOrange = Color(0xFFFF9800)
        val warningRed = Color(0xFFB71C1C)
        val warningYellow = Color(0xFFFFD54F)
        val outlineColor = Color(0xFF2D2D2D)
        val arrowGreen = Color(0xFF8BC34A)
        val arrowBlue = Color(0xFF4FC3F7)
        val lightningYellow = Color(0xFFFFC107)
        val cloudGray = Color(0xFFE0E0E0)

        // Draw Laptop (Top Left)
        withTransform({
            translate(left = canvasWidth * 0.05f, top = canvasHeight * 0.15f)
            rotate(degrees = -10f, pivot = Offset(canvasWidth * 0.15f, canvasHeight * 0.15f))
        }) {
            val lW = canvasWidth * 0.38f
            val lH = canvasHeight * 0.28f
            
            // Screen
            drawRoundRect(
                color = laptopBody,
                size = Size(lW, lH),
                cornerRadius = CornerRadius(15f)
            )
            drawRoundRect(
                color = outlineColor,
                size = Size(lW, lH),
                cornerRadius = CornerRadius(15f),
                style = Stroke(width = 4f)
            )
            drawRoundRect(
                color = screenBlue,
                topLeft = Offset(lW * 0.08f, lH * 0.08f),
                size = Size(lW * 0.84f, lH * 0.75f),
                cornerRadius = CornerRadius(10f)
            )

            // Scared face on laptop
            drawCircle(Color.White, radius = lW * 0.08f, center = Offset(lW * 0.35f, lH * 0.35f))
            drawCircle(outlineColor, radius = lW * 0.08f, center = Offset(lW * 0.35f, lH * 0.35f), style = Stroke(width = 2f))
            drawCircle(outlineColor, radius = lW * 0.025f, center = Offset(lW * 0.35f, lH * 0.35f))

            drawCircle(Color.White, radius = lW * 0.08f, center = Offset(lW * 0.65f, lH * 0.35f))
            drawCircle(outlineColor, radius = lW * 0.08f, center = Offset(lW * 0.65f, lH * 0.35f), style = Stroke(width = 2f))
            drawCircle(outlineColor, radius = lW * 0.025f, center = Offset(lW * 0.65f, lH * 0.35f))

            val mouthPath = Path().apply {
                moveTo(lW * 0.35f, lH * 0.65f)
                quadraticTo(lW * 0.5f, lH * 0.85f, lW * 0.65f, lH * 0.65f)
                close()
            }
            drawPath(mouthPath, outlineColor)

            // Keyboard base
            val kPath = Path().apply {
                moveTo(0f, lH)
                lineTo(-lW * 0.15f, lH + lH * 0.25f)
                lineTo(lW * 1.15f, lH + lH * 0.25f)
                lineTo(lW, lH)
                close()
            }
            drawPath(kPath, laptopBody)
            drawPath(kPath, outlineColor, style = Stroke(width = 4f, join = StrokeJoin.Round))
        }

        // Draw Smartphone (Top Right)
        withTransform({
            translate(left = canvasWidth * 0.65f, top = canvasHeight * 0.18f)
            rotate(degrees = 15f, pivot = Offset(canvasWidth * 0.1f, canvasHeight * 0.15f))
        }) {
            val pW = canvasWidth * 0.25f
            val pH = canvasHeight * 0.4f
            
            drawRoundRect(
                color = laptopBody,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(25f)
            )
            drawRoundRect(
                color = outlineColor,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(25f),
                style = Stroke(width = 4f)
            )
            drawRoundRect(
                color = screenBlue,
                topLeft = Offset(pW * 0.1f, pH * 0.08f),
                size = Size(pW * 0.8f, pH * 0.8f),
                cornerRadius = CornerRadius(15f)
            )

            // Scared face on phone
            drawCircle(Color.White, radius = pW * 0.15f, center = Offset(pW * 0.35f, pH * 0.35f))
            drawCircle(outlineColor, radius = pW * 0.15f, center = Offset(pW * 0.35f, pH * 0.35f), style = Stroke(width = 2f))
            drawCircle(outlineColor, radius = pW * 0.04f, center = Offset(pW * 0.35f, pH * 0.35f))

            drawCircle(Color.White, radius = pW * 0.15f, center = Offset(pW * 0.65f, pH * 0.35f))
            drawCircle(outlineColor, radius = pW * 0.15f, center = Offset(pW * 0.65f, pH * 0.35f), style = Stroke(width = 2f))
            drawCircle(outlineColor, radius = pW * 0.04f, center = Offset(pW * 0.65f, pH * 0.35f))

            val mouthPath = Path().apply {
                moveTo(pW * 0.35f, pH * 0.65f)
                quadraticTo(pW * 0.5f, pH * 0.85f, pW * 0.65f, pH * 0.65f)
                close()
            }
            drawPath(mouthPath, outlineColor)
        }

        // Draw Sync Arrows (Center)
        val centerX = canvasWidth * 0.5f
        val centerY = canvasHeight * 0.35f
        val arrowRadius = canvasWidth * 0.22f

        withTransform({
            translate(centerX, centerY)
        }) {
            val topArrowPath = Path().apply {
                arcTo(
                    rect = Rect(-arrowRadius, -arrowRadius * 0.6f, arrowRadius, arrowRadius * 0.6f),
                    startAngleDegrees = 200f,
                    sweepAngleDegrees = 140f,
                    forceMoveTo = false
                )
            }
            drawPath(topArrowPath, arrowGreen, style = Stroke(width = 25f, cap = StrokeCap.Round))
            
            val bottomArrowPath = Path().apply {
                arcTo(
                    rect = Rect(-arrowRadius, -arrowRadius * 0.6f, arrowRadius, arrowRadius * 0.6f),
                    startAngleDegrees = 20f,
                    sweepAngleDegrees = 140f,
                    forceMoveTo = false
                )
            }
            drawPath(bottomArrowPath, arrowBlue, style = Stroke(width = 25f, cap = StrokeCap.Round))
        }

        // Draw Warning Triangle (Bottom Center)
        withTransform({
            translate(centerX, canvasHeight * 0.75f)
        }) {
            val tW = canvasWidth * 0.5f
            val tH = canvasHeight * 0.4f
            
            val trianglePath = Path().apply {
                moveTo(0f, -tH * 0.5f)
                lineTo(tW * 0.5f, tH * 0.5f)
                lineTo(-tW * 0.5f, tH * 0.5f)
                close()
            }
            drawPath(trianglePath, warningOrange)
            drawPath(trianglePath, warningRed, style = Stroke(width = 12f, join = StrokeJoin.Round))
            
            // Exclamation Mark
            val exTop = Path().apply {
                moveTo(0f, -tH * 0.3f)
                lineTo(0f, tH * 0.1f)
            }
            drawPath(exTop, warningRed, style = Stroke(width = 15f, cap = StrokeCap.Round))
            
            // Face on triangle
            drawCircle(Color.White, radius = tW * 0.08f, center = Offset(-tW * 0.15f, tH * 0.25f))
            drawCircle(outlineColor, radius = tW * 0.02f, center = Offset(-tW * 0.15f, tH * 0.25f))
            drawCircle(Color.White, radius = tW * 0.08f, center = Offset(tW * 0.15f, tH * 0.25f))
            drawCircle(outlineColor, radius = tW * 0.02f, center = Offset(tW * 0.15f, tH * 0.25f))
            
            val mouthPath = Path().apply {
                moveTo(-tW * 0.1f, tH * 0.4f)
                quadraticTo(0f, tH * 0.5f, tW * 0.1f, tH * 0.4f)
                close()
            }
            drawPath(mouthPath, outlineColor)
        }

        // Decorative lightning bolts
        fun drawLightning(offset: Offset, rotation: Float, size: Float) {
            withTransform({
                translate(offset.x, offset.y)
                rotate(rotation)
            }) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size * 0.5f, size * 0.2f)
                    lineTo(size * 0.3f, size * 0.5f)
                    lineTo(size, size * 0.8f)
                    lineTo(size * 0.4f, size * 0.6f)
                    lineTo(size * 0.6f, size * 0.3f)
                    close()
                }
                drawPath(path, lightningYellow)
                drawPath(path, outlineColor, style = Stroke(width = 2f))
            }
        }

        drawLightning(Offset(canvasWidth * 0.15f, canvasHeight * 0.05f), 15f, 60f)
        drawLightning(Offset(canvasWidth * 0.8f, canvasHeight * 0.08f), -10f, 70f)
        drawLightning(Offset(canvasWidth * 0.3f, canvasHeight * 0.6f), 45f, 50f)
        drawLightning(Offset(canvasWidth * 0.7f, canvasHeight * 0.65f), -30f, 55f)
        
        // Clouds/Smoke
        fun drawCloud(offset: Offset, size: Float) {
            withTransform({
                translate(offset.x, offset.y)
            }) {
                drawCircle(cloudGray, radius = size * 0.5f, center = Offset(0f, 0f))
                drawCircle(cloudGray, radius = size * 0.4f, center = Offset(-size * 0.4f, size * 0.1f))
                drawCircle(cloudGray, radius = size * 0.4f, center = Offset(size * 0.4f, size * 0.1f))
            }
        }
        
        drawCloud(Offset(canvasWidth * 0.1f, canvasHeight * 0.65f), 40f)
        drawCloud(Offset(canvasWidth * 0.9f, canvasHeight * 0.55f), 35f)
    }
}

@Preview(showBackground = true)
@Composable
fun SyncErrorIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp)
        ) {
            FailedIllustration(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
