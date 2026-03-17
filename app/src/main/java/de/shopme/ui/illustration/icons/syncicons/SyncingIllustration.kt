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
import de.shopme.ui.theme.ShopMeTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SyncingIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.data_transfer_illustration_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val w = size.width
        val h = size.height
        val outlineColor = Color(0xFF3D2B1F)

        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            radius = w * 0.25f,
            center = Offset(w * 0.5f, h * 0.9f)
        )

        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.45f),
            radiusX = w * 0.35f,
            radiusY = h * 0.25f,
            startAngle = 190f,
            sweepAngle = 160f,
            color = Color(0xFF4FC3F7),
            outlineColor = outlineColor,
            isTop = true
        )

        drawCurvedArrow(
            center = Offset(w * 0.5f, h * 0.55f),
            radiusX = w * 0.35f,
            radiusY = h * 0.25f,
            startAngle = 10f,
            sweepAngle = 160f,
            color = Color(0xFF9CCC65),
            outlineColor = outlineColor,
            isTop = false
        )

        withTransform({
            translate(w * 0.25f, h * 0.5f)
            rotate(-15f)
        }) {
            drawPhone(
                width = w * 0.3f,
                height = h * 0.55f,
                bodyColor = Color(0xFF332E2C),
                screenColor = Color(0xFF81D4FA),
                outlineColor = outlineColor
            )
        }

        withTransform({
            translate(w * 0.75f, h * 0.48f)
            rotate(10f)
        }) {
            drawPhone(
                width = w * 0.3f,
                height = h * 0.55f,
                bodyColor = Color(0xFFF5EFE1),
                screenColor = Color(0xFF81D4FA),
                outlineColor = outlineColor
            )
        }

        drawDecoration(w, h, outlineColor)
    }
}

private fun DrawScope.drawPhone(
    width: Float,
    height: Float,
    bodyColor: Color,
    screenColor: Color,
    outlineColor: Color
) {
    val rect = Rect(-width / 2, -height / 2, width / 2, height / 2)
    val cornerRadius = CornerRadius(width * 0.15f)

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

    val screenRect = Rect(
        -width * 0.42f,
        -height * 0.42f,
        width * 0.42f,
        height * 0.3f
    )
    drawRoundRect(
        color = screenColor,
        topLeft = screenRect.topLeft,
        size = screenRect.size,
        cornerRadius = CornerRadius(width * 0.05f)
    )
    drawRoundRect(
        color = outlineColor,
        topLeft = screenRect.topLeft,
        size = screenRect.size,
        cornerRadius = CornerRadius(width * 0.05f),
        style = Stroke(width = 4f)
    )

    drawCloudCharacter(
        center = Offset(0f, -height * 0.05f),
        size = width * 0.7f,
        outlineColor = outlineColor
    )

    drawCircle(
        color = outlineColor.copy(alpha = 0.2f),
        radius = width * 0.08f,
        center = Offset(0f, height * 0.4f)
    )
    drawCircle(
        color = outlineColor,
        radius = width * 0.08f,
        center = Offset(0f, height * 0.4f),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawCloudCharacter(
    center: Offset,
    size: Float,
    outlineColor: Color
) {
    withTransform({
        translate(center.x, center.y)
    }) {
        val cloudPath = Path().apply {
            moveTo(-size * 0.4f, 0f)
            cubicTo(-size * 0.5f, -size * 0.3f, -size * 0.2f, -size * 0.5f, 0f, -size * 0.4f)
            cubicTo(size * 0.2f, -size * 0.5f, size * 0.5f, -size * 0.3f, size * 0.4f, 0f)
            cubicTo(size * 0.5f, size * 0.3f, size * 0.2f, size * 0.5f, 0f, size * 0.4f)
            cubicTo(-size * 0.2f, size * 0.5f, -size * 0.5f, size * 0.3f, -size * 0.4f, 0f)
            close()
        }
        drawPath(cloudPath, Color.White)
        drawPath(cloudPath, outlineColor.copy(alpha = 0.1f), style = Stroke(width = 2f))

        val eyeRadius = size * 0.12f
        drawCircle(Color.Black, radius = eyeRadius, center = Offset(-size * 0.15f, -size * 0.05f))
        drawCircle(Color.Black, radius = eyeRadius, center = Offset(size * 0.15f, -size * 0.05f))
        drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(-size * 0.18f, -size * 0.08f))
        drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(size * 0.12f, -size * 0.08f))

        val mouthPath = Path().apply {
            moveTo(-size * 0.15f, size * 0.1f)
            quadraticTo(0f, size * 0.3f, size * 0.15f, size * 0.1f)
            quadraticTo(0f, size * 0.15f, -size * 0.15f, size * 0.1f)
            close()
        }
        drawPath(mouthPath, Color(0xFFE57373))
        drawPath(mouthPath, outlineColor, style = Stroke(width = 3f))
    }
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
        style = Stroke(width = 50f, cap = StrokeCap.Round)
    )
    drawPath(
        path = arrowPath,
        color = outlineColor,
        style = Stroke(width = 54f, cap = StrokeCap.Round),
        blendMode = BlendMode.DstOver
    )

    val headCenterAngle = if (isTop) startAngle + sweepAngle else startAngle
    val headX = center.x + radiusX * cos(Math.toRadians(headCenterAngle.toDouble())).toFloat()
    val headY = center.y + radiusY * sin(Math.toRadians(headCenterAngle.toDouble())).toFloat()

    withTransform({
        translate(headX, headY)
        rotate(if (isTop) sweepAngle + 100 else sweepAngle - 80)
    }) {
        val headPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(-40f, -40f)
            lineTo(-40f, 40f)
            close()
        }
        drawPath(headPath, color)
        drawPath(headPath, outlineColor, style = Stroke(width = 6f, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawDecoration(w: Float, h: Float, outlineColor: Color) {
    drawStar(Offset(w * 0.15f, h * 0.25f), Color(0xFFFFD54F), 20f, outlineColor)
    drawStar(Offset(w * 0.85f, h * 0.3f), Color(0xFFFFD54F), 25f, outlineColor)
    drawStar(Offset(w * 0.45f, h * 0.28f), Color(0xFFFFD54F), 30f, outlineColor)
    drawStar(Offset(w * 0.55f, h * 0.65f), Color(0xFFFFD54F), 22f, outlineColor)
    drawStar(Offset(w * 0.2f, h * 0.85f), Color(0xFFFFD54F), 18f, outlineColor)
    drawStar(Offset(w * 0.88f, h * 0.82f), Color(0xFFFFD54F), 20f, outlineColor)

    drawDiamond(Offset(w * 0.4f, h * 0.4f), Color(0xFFFFD54F), 35f, outlineColor)
    drawDiamond(Offset(w * 0.5f, h * 0.45f), Color(0xFF4FC3F7), 15f, outlineColor)
    drawDiamond(Offset(w * 0.78f, h * 0.85f), Color(0xFF4FC3F7), 12f, outlineColor)

    drawCircle(Color(0xFFE57373), radius = 6f, center = Offset(w * 0.55f, h * 0.55f))
    drawCircle(Color(0xFF81C784), radius = 5f, center = Offset(w * 0.5f, h * 0.7f))
    drawCircle(Color(0xFF81C784), radius = 5f, center = Offset(w * 0.18f, h * 0.85f))
    drawCircle(Color(0xFFE57373), radius = 6f, center = Offset(w * 0.48f, h * 0.78f))
}

private fun DrawScope.drawStar(offset: Offset, color: Color, size: Float, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
    }) {
        val path = Path().apply {
            moveTo(0f, -size)
            quadraticTo(size * 0.1f, -size * 0.1f, size, 0f)
            quadraticTo(size * 0.1f, size * 0.1f, 0f, size)
            quadraticTo(-size * 0.1f, size * 0.1f, -size, 0f)
            quadraticTo(-size * 0.1f, -size * 0.1f, 0f, -size)
            close()
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 2f))
    }
}

private fun DrawScope.drawDiamond(offset: Offset, color: Color, size: Float, outlineColor: Color) {
    withTransform({
        translate(offset.x, offset.y)
        rotate(45f)
    }) {
        val path = Path().apply {
            addRoundRect(RoundRect(Rect(-size / 2, -size / 2, size / 2, size / 2), CornerRadius(size * 0.2f)))
        }
        drawPath(path, color)
        drawPath(path, outlineColor, style = Stroke(width = 2f))
    }
}

@Preview(showBackground = true)
@Composable
fun DataTransferIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp)
        ) {
            SyncingIllustration(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
