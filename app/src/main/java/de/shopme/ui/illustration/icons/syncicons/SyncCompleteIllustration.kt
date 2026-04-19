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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SyncCompleteIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.data_transfer_complete_illustration_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val w = size.width
        val h = size.height
        val outlineColor = Color(0xFF3D2B1F)

        // Shadow under phones
        drawOval(
            color = BrandBlack.copy(alpha = 0.05f),
            topLeft = Offset(w * 0.2f, h * 0.85f),
            size = Size(w * 0.6f, h * 0.1f)
        )

        // Green Top Arrow
        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.42f),
            radiusX = w * 0.38f,
            radiusY = h * 0.28f,
            startAngle = 190f,
            sweepAngle = 160f,
            color = Color(0xFF8BC34A),
            outlineColor = outlineColor,
            isTop = true
        )

        // Orange Bottom Arrow
        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.58f),
            radiusX = w * 0.38f,
            radiusY = h * 0.28f,
            startAngle = 10f,
            sweepAngle = 160f,
            color = Color(0xFFFFB300),
            outlineColor = outlineColor,
            isTop = false
        )

        // Blue Phone (Left)
        withTransform({
            translate(w * 0.28f, h * 0.52f)
            rotate(-12f)
        }) {
            drawPhone(
                width = w * 0.28f,
                height = h * 0.52f,
                bodyColor = Color(0xFF1E3A5F),
                screenColor = Color(0xFF81D4FA),
                outlineColor = outlineColor,
                isBlue = true
            )
        }

        // Red Phone (Right)
        withTransform({
            translate(w * 0.72f, h * 0.5f)
            rotate(8f)
        }) {
            drawPhone(
                width = w * 0.28f,
                height = h * 0.52f,
                bodyColor = Color(0xFFB71C1C),
                screenColor = Color(0xFFFFD54F),
                outlineColor = outlineColor,
                isBlue = false
            )
        }

        // Central Checkmark Button
        drawCentralCheckmark(
            center = Offset(w * 0.5f, h * 0.5f),
            size = w * 0.22f,
            outlineColor = outlineColor
        )

        drawDecorations(w, h, outlineColor)
    }
}

private fun DrawScope.drawPhone(
    width: Float,
    height: Float,
    bodyColor: Color,
    screenColor: Color,
    outlineColor: Color,
    isBlue: Boolean
) {
    val rect = Rect(-width / 2, -height / 2, width / 2, height / 2)
    val cornerRadius = CornerRadius(width * 0.18f)

    // Body
    drawRoundRect(
        color = bodyColor,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = cornerRadius
    )
    drawRoundRect(
        color = outlineColor,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 6f)
    )

    // Screen
    val screenInset = width * 0.08f
    val screenRect = Rect(
        -width / 2 + screenInset,
        -height / 2 + screenInset,
        width / 2 - screenInset,
        height / 2 - screenInset * 1.5f
    )
    drawRoundRect(
        color = screenColor,
        topLeft = screenRect.topLeft,
        size = screenRect.size,
        cornerRadius = CornerRadius(width * 0.1f)
    )
    drawRoundRect(
        color = outlineColor,
        topLeft = screenRect.topLeft,
        size = screenRect.size,
        cornerRadius = CornerRadius(width * 0.1f),
        style = Stroke(width = 4f)
    )

    // Face
    val eyeY = -height * 0.15f
    val eyeSpacing = width * 0.18f
    if (isBlue) {
        // Winking eye (left)
        drawCircle(outlineColor, radius = 6f, center = Offset(-eyeSpacing, eyeY))
        // Eye (right)
        val eyePath = Path().apply {
            moveTo(eyeSpacing - 15f, eyeY)
            quadraticTo(eyeSpacing, eyeY - 10f, eyeSpacing + 15f, eyeY)
        }
        drawPath(eyePath, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
    } else {
        // Two open eyes
        drawCircle(outlineColor, radius = 6f, center = Offset(-eyeSpacing, eyeY))
        drawCircle(outlineColor, radius = 6f, center = Offset(eyeSpacing, eyeY))
    }

    // Mouth
    val mouthY = height * 0.05f
    val mouthPath = Path().apply {
        moveTo(-width * 0.15f, mouthY)
        quadraticTo(0f, mouthY + height * 0.15f, width * 0.15f, mouthY)
        quadraticTo(0f, mouthY + height * 0.05f, -width * 0.15f, mouthY)
        close()
    }
    drawPath(mouthPath, BrandWhite)
    drawPath(mouthPath, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
    
    // Tongue
    withTransform({
        clipPath(mouthPath)
    }) {
        drawCircle(Color(0xFFE57373), radius = width * 0.1f, center = Offset(0f, mouthY + height * 0.12f))
    }

    // Bottom item
    if (isBlue) {
        // Green check box
        val boxSize = width * 0.25f
        val boxTopLeft = Offset(-boxSize / 2, height * 0.2f)
        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = boxTopLeft,
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(boxSize * 0.2f)
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = boxTopLeft,
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(boxSize * 0.2f),
            style = Stroke(width = 3f)
        )
        val checkPath = Path().apply {
            moveTo(boxTopLeft.x + boxSize * 0.25f, boxTopLeft.y + boxSize * 0.5f)
            lineTo(boxTopLeft.x + boxSize * 0.45f, boxTopLeft.y + boxSize * 0.7f)
            lineTo(boxTopLeft.x + boxSize * 0.75f, boxTopLeft.y + boxSize * 0.3f)
        }
        drawPath(checkPath, BrandWhite, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    } else {
        // Chat bubble
        val bubbleW = width * 0.45f
        val bubbleH = width * 0.28f
        val bubbleTopLeft = Offset(-bubbleW / 2, height * 0.2f)
        val bubblePath = Path().apply {
            addRoundRect(RoundRect(Rect(bubbleTopLeft, Size(bubbleW, bubbleH)), CornerRadius(bubbleH * 0.4f)))
            moveTo(0f, height * 0.2f + bubbleH)
            lineTo(-10f, height * 0.2f + bubbleH + 10f)
            lineTo(10f, height * 0.2f + bubbleH)
            close()
        }
        drawPath(bubblePath, BrandWhite)
        drawPath(bubblePath, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
        
        // Dots
        for (i in -1..1) {
            drawCircle(Color.LightGray, radius = 3f, center = Offset(i * 12f, height * 0.2f + bubbleH * 0.5f))
        }
    }
}

private fun DrawScope.drawCentralCheckmark(
    center: Offset,
    size: Float,
    outlineColor: Color
) {
    drawCircle(
        color = Color(0xFF4CAF50),
        radius = size / 2,
        center = center
    )
    drawCircle(
        color = outlineColor,
        radius = size / 2,
        center = center,
        style = Stroke(width = 6f)
    )

    val checkPath = Path().apply {
        moveTo(center.x - size * 0.22f, center.y + size * 0.05f)
        lineTo(center.x - size * 0.05f, center.y + size * 0.22f)
        lineTo(center.x + size * 0.25f, center.y - size * 0.15f)
    }
    drawPath(
        path = checkPath,
        color = BrandWhite,
        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCurvedArrow(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color,
    outlineColor: Color,
    isTop: Boolean
) {
    val arrowPath = Path().apply {
        arcTo(
            rect = Rect(center.x - radiusX, center.y - radiusY, center.x + radiusX, center.y + radiusY),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = sweepAngle,
            forceMoveTo = false
        )
    }
    
    drawPath(
        path = arrowPath,
        color = color,
        style = Stroke(width = 40f, cap = StrokeCap.Round)
    )
    drawPath(
        path = arrowPath,
        color = outlineColor,
        style = Stroke(width = 46f, cap = StrokeCap.Round),
        blendMode = BlendMode.DstOver
    )

    val headCenterAngle = if (isTop) startAngle + sweepAngle else startAngle
    val headX = center.x + radiusX * cos(Math.toRadians(headCenterAngle.toDouble())).toFloat()
    val headY = center.y + radiusY * sin(Math.toRadians(headCenterAngle.toDouble())).toFloat()

    withTransform({
        translate(headX, headY)
        rotate(if (isTop) sweepAngle + 95 else sweepAngle - 85)
    }) {
        val headPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(-30f, -30f)
            lineTo(-30f, 30f)
            close()
        }
        drawPath(headPath, color)
        drawPath(headPath, outlineColor, style = Stroke(width = 6f, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawDecorations(w: Float, h: Float, outlineColor: Color) {
    // Cloud
    drawCloud(Offset(w * 0.15f, h * 0.2f), w * 0.12f, Color(0xFFBBDEFB), outlineColor)
    
    // Gears
    drawGear(Offset(w * 0.35f, h * 0.15f), 30f, Color(0xFF1E88E5), outlineColor)
    drawGear(Offset(w * 0.88f, h * 0.75f), 35f, Color(0xFF1E88E5), outlineColor)
    drawGear(Offset(w * 0.85f, h * 0.88f), 25f, Color(0xFFFBC02D), outlineColor)

    // Music notes
    drawMusicNote(Offset(w * 0.55f, h * 0.28f), Color(0xFF1E88E5), outlineColor)
    drawMusicNote(Offset(w * 0.85f, h * 0.22f), Color(0xFFF06292), outlineColor)

    // Sun
    drawCircle(Color(0xFFFFD54F), radius = 15f, center = Offset(w * 0.5f, h * 0.25f))

    // Stars
    drawStar(Offset(w * 0.08f, h * 0.38f), Color(0xFFFFB300), 12f, outlineColor)
    drawStar(Offset(w * 0.92f, h * 0.42f), Color(0xFFFFB300), 15f, outlineColor)
    drawStar(Offset(w * 0.95f, h * 0.48f), Color(0xFFF06292), 10f, outlineColor)
    drawStar(Offset(w * 0.22f, h * 0.88f), Color(0xFFFFB300), 12f, outlineColor)
}

private fun DrawScope.drawCloud(offset: Offset, size: Float, color: Color, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
    }) {
        val path = Path().apply {
            moveTo(-size * 0.5f, 0f)
            cubicTo(-size * 0.6f, -size * 0.4f, -size * 0.2f, -size * 0.6f, 0f, -size * 0.4f)
            cubicTo(size * 0.3f, -size * 0.6f, size * 0.7f, -size * 0.3f, size * 0.5f, 0f)
            cubicTo(size * 0.7f, size * 0.3f, size * 0.2f, size * 0.5f, 0f, size * 0.3f)
            cubicTo(-size * 0.3f, size * 0.5f, -size * 0.7f, size * 0.3f, -size * 0.5f, 0f)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 2f))
    }
}

private fun DrawScope.drawGear(offset: Offset, radius: Float, color: Color, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
        rotate(15f)
    }) {
        val path = Path()
        val teeth = 8
        val innerRadius = radius * 0.7f
        val toothHeight = radius * 0.3f
        
        for (i in 0 until teeth) {
            val angle = (i * 360f / teeth).toDouble()
            
            val x1 = (innerRadius * cos(Math.toRadians(angle))).toFloat()
            val y1 = (innerRadius * sin(Math.toRadians(angle))).toFloat()
            val x2 = ((innerRadius + toothHeight) * cos(Math.toRadians(angle + 5))).toFloat()
            val y2 = ((innerRadius + toothHeight) * sin(Math.toRadians(angle + 5))).toFloat()
            val x3 = ((innerRadius + toothHeight) * cos(Math.toRadians(angle + 40))).toFloat()
            val y3 = ((innerRadius + toothHeight) * sin(Math.toRadians(angle + 40))).toFloat()
            val x4 = (innerRadius * cos(Math.toRadians(angle + 45))).toFloat()
            val y4 = (innerRadius * sin(Math.toRadians(angle + 45))).toFloat()

            if (i == 0) path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)
        }
        path.close()
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
        drawCircle(color = BrandWhite, radius = radius * 0.3f, center = Offset.Zero)
        drawCircle(color = outlineColor, radius = radius * 0.3f, center = Offset.Zero, style = Stroke(width = 3f))
    }
}

private fun DrawScope.drawMusicNote(offset: Offset, color: Color, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
    }) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, -40f)
            lineTo(20f, -30f)
            lineTo(20f, 0f)
            moveTo(20f, -30f)
            lineTo(20f, -30f)
        }
        drawCircle(color, radius = 10f, center = Offset(-5f, 0f))
        drawCircle(outlineColor, radius = 10f, center = Offset(-5f, 0f), style = Stroke(width = 3f))
        drawPath(path, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawStar(offset: Offset, color: Color, size: Float, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
    }) {
        val path = Path().apply {
            moveTo(0f, -size)
            quadraticTo(size * 0.2f, -size * 0.2f, size, 0f)
            quadraticTo(size * 0.2f, size * 0.2f, 0f, size)
            quadraticTo(-size * 0.2f, size * 0.2f, -size, 0f)
            quadraticTo(-size * 0.2f, -size * 0.2f, 0f, -size)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 2f))
    }
}

@Preview(showBackground = true)
@Composable
fun SyncCompleteIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp)
    ) {
        SyncCompleteIllustration(
            modifier = Modifier.fillMaxSize()
        )
    }
}
