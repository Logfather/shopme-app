package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@OptIn(ExperimentalTextApi::class)
@Composable
fun EdekaLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.edeka_logo_description)
    val textMeasurer = rememberTextMeasurer()
    val edekaBlue = Color(0xFF00529F)
    val edekaYellow = Color(0xFFFEE100)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = diameter / 2

        // Outer button shading (bottom-right dark)
        drawCircle(
            color = Color(0xFFC7AF00),
            radius = radius,
            center = center
        )

        // Main yellow circle
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFF380), edekaYellow, Color(0xFFE5CB00)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            ),
            radius = radius * 0.97f,
            center = center
        )

        // Draw the big 'E'
        drawEdekaE(center, radius, edekaBlue)

        // Draw "EDEKA" text
        val textStyle = TextStyle(
            color = edekaBlue,
            fontSize = (radius * 0.28f).toSp(),
            fontWeight = FontWeight.Black,
            letterSpacing = (radius * 0.01f).toSp()
        )
        val textLayoutResult = textMeasurer.measure(
            text = "EDEKA",
            style = textStyle
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                center.x - textLayoutResult.size.width / 2,
                center.y + radius * 0.42f
            )
        )
    }
}

private fun DrawScope.drawEdekaE(center: Offset, radius: Float, color: Color) {
    val eWidth = radius * 1.05f
    val eHeight = radius * 0.85f
    val barThickness = eHeight * 0.25f
    val verticalBarWidth = eWidth * 0.35f

    // Shift 'E' upwards
    val startX = center.x - eWidth / 2
    val startY = center.y - eHeight / 2 - radius * 0.2f

    // Vertical bar
    drawRect(
        color = color,
        topLeft = Offset(startX, startY),
        size = Size(verticalBarWidth, eHeight)
    )

    // Top horizontal bar
    drawRect(
        color = color,
        topLeft = Offset(startX, startY),
        size = Size(eWidth, barThickness)
    )

    // Middle horizontal bar
    drawRect(
        color = color,
        topLeft = Offset(startX, startY + (eHeight - barThickness) / 2),
        size = Size(eWidth, barThickness)
    )

    // Bottom horizontal bar
    drawRect(
        color = color,
        topLeft = Offset(startX, startY + eHeight - barThickness),
        size = Size(eWidth, barThickness)
    )
}

@Preview(showBackground = true)
@Composable
private fun EdekaLogoPreview() {
    ShopMeTheme {
        EdekaLogo(
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp)
        )
    }
}
