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
import androidx.compose.ui.geometry.RoundRect
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
fun SyncedIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.sync_success_illustration_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val bluePhoneBody = Color(0xFF2B3D4F)
        val bluePhoneScreen = Color(0xFF80D8FF)
        val redPhoneBody = Color(0xFFB71C1C)
        val redPhoneScreen = Color(0xFFFFD54F)
        val greenArrow = Color(0xFF8BC34A)
        val orangeArrow = Color(0xFFFF9800)
        val checkGreen = Color(0xFF4CAF50)
        val outlineColor = Color(0xFF2D2D2D)
        val gearBlue = Color(0xFF5C6BC0)
        val gearOrange = Color(0xFFFFA726)
        val notePink = Color(0xFFF06292)
        val noteBlue = Color(0xFF4FC3F7)
        val cloudColor = Color(0xFFE3F2FD)

        withTransform({
            translate(left = canvasWidth * 0.1f, top = canvasHeight * 0.15f)
        }) {
            val cloudPath = Path().apply {
                moveTo(20f, 40f)
                cubicTo(0f, 40f, 0f, 10f, 20f, 10f)
                cubicTo(20f, -10f, 50f, -10f, 60f, 10f)
                cubicTo(80f, 10f, 80f, 40f, 60f, 40f)
                close()
            }
            drawPath(cloudPath, cloudColor)
            drawPath(cloudPath, outlineColor.copy(alpha = 0.2f), style = Stroke(width = 2f))
        }

        val centerX = canvasWidth * 0.5f
        val centerY = canvasHeight * 0.45f
        val arrowRadius = canvasWidth * 0.35f

        withTransform({
            translate(centerX, centerY)
        }) {
            val arrowPath = Path().apply {
                arcTo(
                    rect = Rect(-arrowRadius, -arrowRadius * 0.8f, arrowRadius, arrowRadius * 0.8f),
                    startAngleDegrees = 200f,
                    sweepAngleDegrees = 140f,
                    forceMoveTo = false
                )
            }
            drawPath(arrowPath, greenArrow, style = Stroke(width = 40f, cap = StrokeCap.Round))
            
            withTransform({
                rotate(degrees = 160f, pivot = Offset(arrowRadius * 0.94f, -arrowRadius * 0.25f))
            }) {
                val head = Path().apply {
                    moveTo(arrowRadius * 0.94f, -arrowRadius * 0.25f)
                    lineTo(arrowRadius * 0.94f - 35f, -arrowRadius * 0.25f - 35f)
                    moveTo(arrowRadius * 0.94f, -arrowRadius * 0.25f)
                    lineTo(arrowRadius * 0.94f - 35f, -arrowRadius * 0.25f + 35f)
                }
                drawPath(head, greenArrow, style = Stroke(width = 40f, cap = StrokeCap.Round))
            }
        }

        withTransform({
            translate(centerX, centerY + canvasHeight * 0.1f)
        }) {
            val arrowPath = Path().apply {
                arcTo(
                    rect = Rect(-arrowRadius, -arrowRadius * 0.8f, arrowRadius, arrowRadius * 0.8f),
                    startAngleDegrees = 20f,
                    sweepAngleDegrees = 140f,
                    forceMoveTo = false
                )
            }
            drawPath(arrowPath, orangeArrow, style = Stroke(width = 40f, cap = StrokeCap.Round))

            withTransform({
                rotate(degrees = -20f, pivot = Offset(-arrowRadius * 0.94f, arrowRadius * 0.25f))
            }) {
                val head = Path().apply {
                    moveTo(-arrowRadius * 0.94f, arrowRadius * 0.25f)
                    lineTo(-arrowRadius * 0.94f + 35f, arrowRadius * 0.25f - 35f)
                    moveTo(-arrowRadius * 0.94f, arrowRadius * 0.25f)
                    lineTo(-arrowRadius * 0.94f + 35f, arrowRadius * 0.25f + 35f)
                }
                drawPath(head, orangeArrow, style = Stroke(width = 40f, cap = StrokeCap.Round))
            }
        }

        withTransform({
            translate(left = canvasWidth * 0.2f, top = canvasHeight * 0.32f)
            rotate(degrees = -10f, pivot = Offset(canvasWidth * 0.15f, canvasHeight * 0.2f))
        }) {
            val pW = canvasWidth * 0.28f
            val pH = canvasHeight * 0.45f
            
            drawRoundRect(
                color = bluePhoneBody,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(30f)
            )
            drawRoundRect(
                color = outlineColor,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(30f),
                style = Stroke(width = 6f)
            )

            drawRoundRect(
                color = bluePhoneScreen,
                topLeft = Offset(pW * 0.1f, pH * 0.08f),
                size = Size(pW * 0.8f, pH * 0.8f),
                cornerRadius = CornerRadius(20f)
            )

            drawCircle(Color.White, radius = pW * 0.12f, center = Offset(pW * 0.35f, pH * 0.35f))
            drawCircle(outlineColor, radius = pW * 0.12f, center = Offset(pW * 0.35f, pH * 0.35f), style = Stroke(width = 3f))
            drawCircle(outlineColor, radius = pW * 0.04f, center = Offset(pW * 0.38f, pH * 0.37f))

            val winkPath = Path().apply {
                moveTo(pW * 0.55f, pH * 0.35f)
                quadraticTo(pW * 0.65f, pH * 0.25f, pW * 0.75f, pH * 0.35f)
            }
            drawPath(winkPath, outlineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

            val mouthPath = Path().apply {
                moveTo(pW * 0.35f, pH * 0.52f)
                quadraticTo(pW * 0.5f, pH * 0.65f, pW * 0.65f, pH * 0.52f)
                close()
            }
            drawPath(mouthPath, Color.White)
            drawPath(mouthPath, outlineColor, style = Stroke(width = 4f, join = StrokeJoin.Round))
            
            withTransform({
                translate(pW * 0.35f, pH * 0.7f)
            }) {
                drawRoundRect(Color(0xFF66BB6A), size = Size(pW * 0.3f, pW * 0.25f), cornerRadius = CornerRadius(10f))
                val check = Path().apply {
                    moveTo(pW * 0.05f, pW * 0.12f)
                    lineTo(pW * 0.12f, pW * 0.18f)
                    lineTo(pW * 0.25f, pW * 0.05f)
                }
                drawPath(check, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        withTransform({
            translate(left = canvasWidth * 0.55f, top = canvasHeight * 0.36f)
            rotate(degrees = 10f, pivot = Offset(canvasWidth * 0.15f, canvasHeight * 0.2f))
        }) {
            val pW = canvasWidth * 0.28f
            val pH = canvasHeight * 0.45f
            
            drawRoundRect(
                color = redPhoneBody,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(30f)
            )
            drawRoundRect(
                color = outlineColor,
                size = Size(pW, pH),
                cornerRadius = CornerRadius(30f),
                style = Stroke(width = 6f)
            )

            drawRoundRect(
                color = redPhoneScreen,
                topLeft = Offset(pW * 0.1f, pH * 0.08f),
                size = Size(pW * 0.8f, pH * 0.8f),
                cornerRadius = CornerRadius(20f)
            )

            drawCircle(Color.White, radius = pW * 0.12f, center = Offset(pW * 0.35f, pH * 0.35f))
            drawCircle(outlineColor, radius = pW * 0.12f, center = Offset(pW * 0.35f, pH * 0.35f), style = Stroke(width = 3f))
            drawCircle(outlineColor, radius = pW * 0.04f, center = Offset(pW * 0.33f, pH * 0.37f))

            drawCircle(Color.White, radius = pW * 0.12f, center = Offset(pW * 0.65f, pH * 0.35f))
            drawCircle(outlineColor, radius = pW * 0.12f, center = Offset(pW * 0.65f, pH * 0.35f), style = Stroke(width = 3f))
            drawCircle(outlineColor, radius = pW * 0.04f, center = Offset(pW * 0.67f, pH * 0.37f))

            val mouthPath = Path().apply {
                moveTo(pW * 0.35f, pH * 0.52f)
                quadraticTo(pW * 0.5f, pH * 0.65f, pW * 0.65f, pH * 0.52f)
                close()
            }
            drawPath(mouthPath, Color.White)
            drawPath(mouthPath, outlineColor, style = Stroke(width = 4f, join = StrokeJoin.Round))

            withTransform({
                translate(pW * 0.22f, pH * 0.72f)
            }) {
                val bubble = Path().apply {
                    addRoundRect(RoundRect(Rect(0f, 0f, pW * 0.56f, pH * 0.12f), CornerRadius(15f)))
                    moveTo(pW * 0.2f, pH * 0.12f)
                    lineTo(pW * 0.25f, pH * 0.18f)
                    lineTo(pW * 0.3f, pH * 0.12f)
                }
                drawPath(bubble, Color.White)
                drawCircle(Color(0xFFFFB74D), radius = 4f, center = Offset(pW * 0.15f, pH * 0.06f))
                drawCircle(Color(0xFFFFB74D), radius = 4f, center = Offset(pW * 0.28f, pH * 0.06f))
                drawCircle(Color(0xFFFFB74D), radius = 4f, center = Offset(pW * 0.41f, pH * 0.06f))
            }
        }

        withTransform({
            translate(centerX, centerY + canvasHeight * 0.04f)
        }) {
            drawCircle(checkGreen, radius = canvasWidth * 0.12f)
            drawCircle(outlineColor, radius = canvasWidth * 0.12f, style = Stroke(width = 6f))
            
            val checkPath = Path().apply {
                moveTo(-canvasWidth * 0.05f, 0f)
                lineTo(-canvasWidth * 0.01f, canvasWidth * 0.04f)
                lineTo(canvasWidth * 0.06f, -canvasWidth * 0.04f)
            }
            drawPath(checkPath, Color.White, style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        fun drawGear(offset: Offset, radius: Float, color: Color, rotation: Float) {
            withTransform({
                translate(offset.x, offset.y)
                rotate(rotation)
            }) {
                drawCircle(color, radius = radius * 0.7f)
                drawCircle(outlineColor, radius = radius * 0.7f, style = Stroke(width = 3f))
                drawCircle(Color.White, radius = radius * 0.2f)
                drawCircle(outlineColor, radius = radius * 0.2f, style = Stroke(width = 2f))
                
                for (i in 0 until 8) {
                    withTransform({
                        rotate(i * 45f)
                    }) {
                        drawRoundRect(color, topLeft = Offset(-radius * 0.15f, -radius), size = Size(radius * 0.3f, radius * 0.4f), cornerRadius = CornerRadius(5f))
                        drawRoundRect(outlineColor, topLeft = Offset(-radius * 0.15f, -radius), size = Size(radius * 0.3f, radius * 0.4f), cornerRadius = CornerRadius(5f), style = Stroke(width = 2f))
                    }
                }
            }
        }

        drawGear(Offset(canvasWidth * 0.35f, canvasHeight * 0.12f), 35f, gearBlue, 15f)
        drawGear(Offset(canvasWidth * 0.88f, canvasHeight * 0.65f), 40f, gearBlue, -10f)
        drawGear(Offset(canvasWidth * 0.84f, canvasHeight * 0.78f), 30f, gearOrange, 25f)

        fun drawNote(offset: Offset, color: Color, rotation: Float) {
            withTransform({
                translate(offset.x, offset.y)
                rotate(rotation)
            }) {
                drawCircle(color, radius = 8f, center = Offset(0f, 20f))
                drawLine(color, start = Offset(8f, 20f), end = Offset(8f, 0f), strokeWidth = 5f)
                drawLine(color, start = Offset(8f, 0f), end = Offset(22f, 5f), strokeWidth = 8f, cap = StrokeCap.Round)
            }
        }

        drawNote(Offset(canvasWidth * 0.55f, canvasHeight * 0.22f), noteBlue, -15f)
        drawNote(Offset(canvasWidth * 0.88f, canvasHeight * 0.18f), notePink, 20f)

        fun drawStar(offset: Offset, color: Color, size: Float) {
            withTransform({
                translate(offset.x, offset.y)
            }) {
                val path = Path().apply {
                    moveTo(0f, -size)
                    lineTo(size * 0.2f, -size * 0.2f)
                    lineTo(size, 0f)
                    lineTo(size * 0.2f, size * 0.2f)
                    lineTo(0f, size)
                    lineTo(-size * 0.2f, size * 0.2f)
                    lineTo(-size, 0f)
                    lineTo(-size * 0.2f, -size * 0.2f)
                    close()
                }
                drawPath(path, color)
            }
        }

        drawStar(Offset(canvasWidth * 0.08f, canvasHeight * 0.38f), Color(0xFFFFD54F), 18f)
        drawStar(Offset(canvasWidth * 0.94f, canvasHeight * 0.42f), Color(0xFFFFD54F), 22f)
        drawStar(Offset(canvasWidth * 0.22f, canvasHeight * 0.88f), Color(0xFFFFD54F), 15f)
    }
}

@Preview(showBackground = true)
@Composable
fun SyncSuccessIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp)
        ) {
            SyncedIllustration(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
