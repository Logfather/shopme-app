package de.shopme.ui.illustration.buttons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandWhite

@Composable
fun CloseActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val redColor = Color(0xFFF14E29)
    val yellowBorder = Color(0xFFFFCC00)
    val darkBlack = Color(0xFF141414)
    val sparkleColor = Color(0xFFFFDD44)
    val contentDesc = stringResource(R.string.close_action_button_description)

    Box(
        modifier = modifier
            .size(200.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            drawCircle(
                color = Color(0xFFBDBDBD),
                radius = radius,
                center = center
            )
            drawCircle(
                color = BrandWhite,
                radius = radius * 0.95f,
                center = center
            )

            drawCircle(
                color = yellowBorder,
                radius = radius * 0.88f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF7E5F), redColor),
                    center = center,
                    radius = radius * 0.83f
                ),
                radius = radius * 0.83f,
                center = center
            )

            drawXPlatform(center, radius * 1.05f, radius * 0.55f, BrandWhite)
            drawXPlatform(center, radius * 1.0f, radius * 0.5f, Color(0xFFECECEC))

            val xThickness = radius * 0.38f
            val xLength = radius * 0.9f
            
            drawX(center, xLength, xThickness, Color(0xFF333333))
            drawX(center, xLength * 0.98f, xThickness * 0.95f, darkBlack)

            withTransform({
                translate(left = -radius * 0.05f, top = -radius * 0.05f)
            }) {
                drawXHighlight(center, xLength * 0.7f, xThickness * 0.35f, BrandWhite.copy(alpha = 0.8f))
            }

            drawSparkle(Offset(center.x - radius * 0.45f, center.y - radius * 0.55f), radius * 0.15f, sparkleColor)
            drawSparkle(Offset(center.x + radius * 0.55f, center.y + radius * 0.55f), radius * 0.12f, sparkleColor)
            
            drawCircle(sparkleColor, radius * 0.04f, Offset(center.x - radius * 0.35f, center.y - radius * 0.65f))
            drawCircle(sparkleColor, radius * 0.03f, Offset(center.x - radius * 0.6f, center.y - radius * 0.3f))
            drawCircle(sparkleColor, radius * 0.025f, Offset(center.x + radius * 0.65f, center.y - radius * 0.1f))
            drawCircle(sparkleColor, radius * 0.035f, Offset(center.x + radius * 0.3f, center.y + radius * 0.65f))
            drawCircle(sparkleColor, radius * 0.02f, Offset(center.x - radius * 0.2f, center.y + radius * 0.75f))
            drawCircle(sparkleColor, radius * 0.03f, Offset(center.x + radius * 0.1f, center.y - radius * 0.78f))
        }
    }
}

private fun DrawScope.drawXPlatform(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
    withTransform({ rotate(45f, center) }) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - thickness / 2, center.y - length / 2),
            size = Size(thickness, length),
            cornerRadius = CornerRadius(thickness / 2)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - length / 2, center.y - thickness / 2),
            size = Size(length, thickness),
            cornerRadius = CornerRadius(thickness / 2)
        )
    }
}

private fun DrawScope.drawX(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
    val halfLength = length / 2
    withTransform({
        rotate(45f, center)
    }) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - thickness / 2, center.y - halfLength),
            size = Size(thickness, length),
            cornerRadius = CornerRadius(thickness / 2)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - halfLength, center.y - thickness / 2),
            size = Size(length, thickness),
            cornerRadius = CornerRadius(thickness / 2)
        )
    }
}

private fun DrawScope.drawXHighlight(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
    withTransform({
        rotate(45f, center)
    }) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - length / 2, center.y - thickness / 2),
            size = Size(length * 0.4f, thickness),
            cornerRadius = CornerRadius(thickness / 2)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - thickness / 2, center.y - length / 2),
            size = Size(thickness, length * 0.4f),
            cornerRadius = CornerRadius(thickness / 2)
        )
    }
}

private fun DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CloseActionButtonPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        CloseActionButton(onClick = {})
    }
}
