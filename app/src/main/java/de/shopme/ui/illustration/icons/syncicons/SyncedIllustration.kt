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
fun SyncedIllustration(
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

        // Shadow under everything
        drawOval(
            color = BrandBlack.copy(alpha = 0.05f),
            topLeft = Offset(w * 0.1f, h * 0.88f),
            size = Size(w * 0.8f, h * 0.08f)
        )

        // Green Top Arrow (pointing right)
        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.42f),
            radiusX = w * 0.35f,
            radiusY = h * 0.25f,
            startAngle = 185f,
            sweepAngle = 170f,
            color = Color(0xFF8BC34A),
            outlineColor = outlineColor,
            isTop = true
        )

        // Orange Bottom Arrow (pointing left)
        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.58f),
            radiusX = w * 0.35f,
            radiusY = h * 0.25f,
            startAngle = 5f,
            sweepAngle = 170f,
            color = Color(0xFFFFB300),
            outlineColor = outlineColor,
            isTop = false
        )

        // Left Phone (Blue)
        withTransform({
            translate(w * 0.3f, h * 0.52f)
            rotate(-10f)
        }) {
            drawPhone(
                width = w * 0.28f,
                height = h * 0.55f,
                bodyColor = Color(0xFF1B3B5A),
                screenColor = Color(0xFF81D4FA),
                outlineColor = outlineColor,
                isBlue = true
            )
        }

        // Right Phone (Red)
        withTransform({
            translate(w * 0.7f, h * 0.5f)
            rotate(8f)
        }) {
            drawPhone(
                width = w * 0.28f,
                height = h * 0.55f,
                bodyColor = Color(0xFF9E1B1B),
                screenColor = Color(0xFFFFD54F),
                outlineColor = outlineColor,
                isBlue = false
            )
        }

        // Central Checkmark Button
        drawCentralCheckmark(
            center = Offset(w * 0.5f, h * 0.5f),
            size = w * 0.25f,
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

    // Outer Body
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
    val screenInset = width * 0.1f
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
    val eyeY = -height * 0.12f
    val eyeSpacing = width * 0.18f
    if (isBlue) {
        // Left eye: dot
        drawCircle(outlineColor, radius = 5f, center = Offset(-eyeSpacing, eyeY))
        // Right eye: wink
        val winkPath = Path().apply {
            moveTo(eyeSpacing - 12f, eyeY - 5f)
            quadraticTo(eyeSpacing, eyeY + 5f, eyeSpacing + 12f, eyeY - 5f)
        }
        drawPath(winkPath, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
    } else {
        // Both eyes: dots
        drawCircle(outlineColor, radius = 5f, center = Offset(-eyeSpacing, eyeY))
        drawCircle(outlineColor, radius = 5f, center = Offset(eyeSpacing, eyeY))
    }

    // Mouth
    val mouthY = height * 0.08f
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

    // Inner UI elements
    if (isBlue) {
        // Green check mark box
        val boxSize = width * 0.28f
        val boxCenter = Offset(0f, height * 0.25f)
        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(boxCenter.x - boxSize/2, boxCenter.y - boxSize/2),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(boxSize * 0.2f)
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(boxCenter.x - boxSize/2, boxCenter.y - boxSize/2),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(boxSize * 0.2f),
            style = Stroke(width = 3f)
        )
        val checkPath = Path().apply {
            moveTo(boxCenter.x - boxSize * 0.2f, boxCenter.y)
            lineTo(boxCenter.x - boxSize * 0.05f, boxCenter.y + boxSize * 0.15f)
            lineTo(boxCenter.x + boxSize * 0.25f, boxCenter.y - boxSize * 0.15f)
        }
        drawPath(checkPath, BrandWhite, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    } else {
        // Chat bubble
        val bubbleW = width * 0.45f
        val bubbleH = width * 0.3f
        val bubbleCenter = Offset(0f, height * 0.25f)
        val bubblePath = Path().apply {
            addRoundRect(RoundRect(Rect(bubbleCenter.x - bubbleW/2, bubbleCenter.y - bubbleH/2, bubbleCenter.x + bubbleW/2, bubbleCenter.y + bubbleH/2), CornerRadius(bubbleH * 0.4f)))
            moveTo(bubbleCenter.x - 5f, bubbleCenter.y + bubbleH/2)
            lineTo(bubbleCenter.x - 12f, bubbleCenter.y + bubbleH/2 + 10f)
            lineTo(bubbleCenter.x + 5f, bubbleCenter.y + bubbleH/2)
        }
        drawPath(bubblePath, BrandWhite)
        drawPath(bubblePath, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
        
        // Chat dots
        for (i in -1..1) {
            drawCircle(Color.LightGray, radius = 3f, center = Offset(bubbleCenter.x + i * 12f, bubbleCenter.y))
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
        moveTo(center.x - size * 0.2f, center.y + size * 0.02f)
        lineTo(center.x - size * 0.02f, center.y + size * 0.2f)
        lineTo(center.x + size * 0.25f, center.y - size * 0.1f)
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

    val headAngle = if (isTop) startAngle + sweepAngle else startAngle
    val headX = center.x + radiusX * cos(Math.toRadians(headAngle.toDouble())).toFloat()
    val headY = center.y + radiusY * sin(Math.toRadians(headAngle.toDouble())).toFloat()

    withTransform({
        translate(headX, headY)
        rotate(if (isTop) 90f else -90f)
    }) {
        val headPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(-25f, -25f)
            lineTo(-25f, 25f)
            close()
        }
        drawPath(headPath, color)
        drawPath(headPath, outlineColor, style = Stroke(width = 6f, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawDecorations(w: Float, h: Float, outlineColor: Color) {
    // Top Left
    drawGear(Offset(w * 0.32f, h * 0.15f), 25f, Color(0xFF1E88E5), outlineColor)
    drawCloud(Offset(w * 0.12f, h * 0.22f), w * 0.15f, Color(0xFFBBDEFB), outlineColor)
    
    // Top Middle
    drawCircle(Color(0xFFFFD54F), radius = 12f, center = Offset(w * 0.5f, h * 0.22f))
    drawMusicNote(Offset(w * 0.58f, h * 0.28f), Color(0xFF1E88E5), outlineColor)
    
    // Top Right
    drawMusicNote(Offset(w * 0.88f, h * 0.22f), Color(0xFFF06292), outlineColor)
    
    // Middle Right
    drawClover(Offset(w * 0.9f, h * 0.55f), 25f, Color(0xFF4CAF50), outlineColor)
    
    // Bottom Right
    drawGear(Offset(w * 0.88f, h * 0.72f), 30f, Color(0xFF1E88E5), outlineColor)
    drawGear(Offset(w * 0.82f, h * 0.85f), 22f, Color(0xFFFBC02D), outlineColor)
    
    // Bottom Left
    drawBasket(Offset(w * 0.12f, h * 0.72f), 35f, Color(0xFFFFB300), outlineColor)

    // Sparkles / Stars
    drawStar(Offset(w * 0.05f, h * 0.38f), Color(0xFFFFB300), 10f, outlineColor)
    drawStar(Offset(w * 0.95f, h * 0.42f), Color(0xFFFFB300), 12f, outlineColor)
    drawStar(Offset(w * 0.2f, h * 0.85f), Color(0xFFFFB300), 10f, outlineColor)
    drawStar(Offset(w * 0.65f, h * 0.88f), Color(0xFFFFB300), 8f, outlineColor)
}

private fun DrawScope.drawCloud(offset: Offset, size: Float, color: Color, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y) }) {
        val path = Path().apply {
            moveTo(-size*0.4f, 0f)
            cubicTo(-size*0.6f, -size*0.3f, -size*0.2f, -size*0.5f, 0f, -size*0.4f)
            cubicTo(size*0.3f, -size*0.5f, size*0.7f, -size*0.2f, size*0.5f, 0f)
            cubicTo(size*0.6f, size*0.3f, size*0.2f, size*0.4f, 0f, size*0.3f)
            cubicTo(-size*0.3f, size*0.4f, -size*0.6f, size*0.2f, -size*0.4f, 0f)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 2f))
    }
}

private fun DrawScope.drawGear(offset: Offset, radius: Float, color: Color, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y); rotate(15f) }) {
        val path = Path()
        val teeth = 8
        for (i in 0 until teeth) {
            val angle = i * 360f / teeth
            val nextAngle = (i + 1) * 360f / teeth
            val rOuter = radius
            val rInner = radius * 0.7f
            val rad = Math.toRadians(angle.toDouble())
            val nextRad = Math.toRadians((angle + 360f/teeth*0.5f).toDouble())
            
            if (i == 0) path.moveTo((rInner * cos(rad)).toFloat(), (rInner * sin(rad)).toFloat())
            path.lineTo((rOuter * cos(rad)).toFloat(), (rOuter * sin(rad)).toFloat())
            path.lineTo((rOuter * cos(nextRad)).toFloat(), (rOuter * sin(nextRad)).toFloat())
            path.lineTo((rInner * cos(nextRad)).toFloat(), (rInner * sin(nextRad)).toFloat())
        }
        path.close()
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
        drawCircle(BrandWhite, radius = radius * 0.25f)
        drawCircle(outlineColor, radius = radius * 0.25f, style = Stroke(width = 3f))
    }
}

private fun DrawScope.drawMusicNote(offset: Offset, color: Color, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y) }) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, -25f)
            quadraticTo(10f, -20f, 15f, -30f)
            lineTo(15f, -10f)
        }
        drawCircle(color, radius = 8f, center = Offset(-5f, 0f))
        drawCircle(outlineColor, radius = 8f, center = Offset(-5f, 0f), style = Stroke(width = 3f))
        drawPath(path, outlineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawClover(offset: Offset, size: Float, color: Color, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y) }) {
        for (i in 0 until 4) {
            withTransform({ rotate(i * 90f) }) {
                drawCircle(color, radius = size * 0.4f, center = Offset(size * 0.4f, 0f))
                drawCircle(outlineColor, radius = size * 0.4f, center = Offset(size * 0.4f, 0f), style = Stroke(width = 2f))
            }
        }
    }
}

private fun DrawScope.drawBasket(offset: Offset, size: Float, color: Color, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y); rotate(-15f) }) {
        val path = Path().apply {
            moveTo(-size/2, -size/4)
            lineTo(size/2, -size/4)
            lineTo(size/3, size/2)
            lineTo(-size/3, size/2)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 3f, join = StrokeJoin.Round))
        // Handle
        val handlePath = Path().apply {
            arcTo(Rect(-size/2, -size/2, size/2, 0f), 180f, 180f, false)
        }
        drawPath(handlePath, outlineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawStar(offset: Offset, color: Color, size: Float, outlineColor: Color) {
    withTransform({ translate(offset.x, offset.y) }) {
        val path = Path().apply {
            moveTo(0f, -size)
            quadraticTo(size * 0.2f, -size * 0.2f, size, 0f)
            quadraticTo(size * 0.2f, size * 0.2f, 0f, size)
            quadraticTo(-size * 0.2f, size * 0.2f, -size, 0f)
            quadraticTo(-size * 0.2f, -size * 0.2f, 0f, -size)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 1.5f))
    }
}

@Preview(showBackground = true)
@Composable
fun SyncedIllustrationPreview() {
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
