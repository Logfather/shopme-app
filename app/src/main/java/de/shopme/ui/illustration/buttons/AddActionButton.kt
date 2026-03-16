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
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun AddActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greenColor = Color(0xFF99CC33)
    val yellowSparkle = Color(0xFFFFCC00)
    val darkBlack = Color(0xFF141414)
    val contentDesc = stringResource(R.string.add_action_button_description)

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

            // Outer dark border
            drawCircle(
                color = Color(0xFF333333),
                radius = radius,
                center = center
            )
            // White border
            drawCircle(
                color = Color.White,
                radius = radius * 0.96f,
                center = center
            )
            // Green background
            drawCircle(
                color = greenColor,
                radius = radius * 0.88f,
                center = center
            )

            // Plus platform (layered white/cream)
            val platformSize = radius * 1.1f
            val platformThickness = radius * 0.55f
            drawPlusPlatform(center, platformSize, platformThickness, Color(0xFFE0E0E0))
            drawPlusPlatform(center, platformSize * 0.95f, platformThickness * 0.92f, Color.White)

            // The Plus sign
            val plusLength = radius * 0.85f
            val plusThickness = radius * 0.42f
            
            // Black body
            drawPlus(center, plusLength, plusThickness, Color(0xFF333333))
            drawPlus(center, plusLength * 0.98f, plusThickness * 0.95f, darkBlack)

            // Highlights on the plus sign
            withTransform({
                translate(left = -radius * 0.03f, top = -radius * 0.03f)
            }) {
                drawPlusHighlight(center, plusLength * 0.7f, plusThickness * 0.3f, Color.White.copy(alpha = 0.6f))
            }
            
            // Glossy spot
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = radius * 0.06f,
                center = Offset(center.x - plusLength * 0.35f, center.y - plusThickness * 0.25f)
            )

            // Sparkles and dots
            drawSparkle(Offset(center.x - radius * 0.4f, center.y - radius * 0.5f), radius * 0.12f, yellowSparkle)
            drawSparkle(Offset(center.x + radius * 0.55f, center.y + radius * 0.5f), radius * 0.14f, yellowSparkle)
            
            drawCircle(yellowSparkle, radius * 0.045f, Offset(center.x - radius * 0.55f, center.y - radius * 0.3f))
            drawCircle(yellowSparkle, radius * 0.035f, Offset(center.x - radius * 0.3f, center.y - radius * 0.65f))
            drawCircle(yellowSparkle, radius * 0.03f, Offset(center.x + radius * 0.55f, center.y - radius * 0.4f))
            drawCircle(yellowSparkle, radius * 0.04f, Offset(center.x + radius * 0.65f, center.y - radius * 0.15f))
            drawCircle(yellowSparkle, radius * 0.035f, Offset(center.x - radius * 0.5f, center.y + radius * 0.55f))
            drawCircle(yellowSparkle, radius * 0.025f, Offset(center.x - radius * 0.4f, center.y + radius * 0.65f))
            drawCircle(yellowSparkle, radius * 0.035f, Offset(center.x + radius * 0.7f, center.y + radius * 0.35f))
            drawCircle(yellowSparkle, radius * 0.02f, Offset(center.x + radius * 0.45f, center.y + radius * 0.7f))
        }
    }
}

private fun DrawScope.drawPlusPlatform(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
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

private fun DrawScope.drawPlus(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
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

private fun DrawScope.drawPlusHighlight(
    center: Offset,
    length: Float,
    thickness: Float,
    color: Color
) {
    // Vertical highlight
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - thickness / 2, center.y - length / 2),
        size = Size(thickness, length * 0.4f),
        cornerRadius = CornerRadius(thickness / 2)
    )
    // Horizontal highlight
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - length / 2, center.y - thickness / 2),
        size = Size(length * 0.4f, thickness),
        cornerRadius = CornerRadius(thickness / 2)
    )
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
private fun AddActionButtonPreview() {
    ShopMeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AddActionButton(onClick = {})
        }
    }
}
