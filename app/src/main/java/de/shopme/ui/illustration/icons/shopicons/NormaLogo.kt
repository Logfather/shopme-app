package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
fun NormaLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.norma_logo_description)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val logoWidth = width * 0.95f
        val logoHeight = logoWidth * 0.45f
        val left = (width - logoWidth) / 2
        val top = centerY - logoHeight / 2

        val yellowColor = Color(0xFFFFC20E)
        val orangeColor = Color(0xFFF39200)
        val redColor = Color(0xFFE4002B)

        drawNormaBackground(left, top, logoWidth, logoHeight, yellowColor, orangeColor, redColor)

        // Draw "NORMA" text
        val textStyle = TextStyle(
            color = Color.White,
            fontSize = (logoHeight * 0.55f).toSp(),
            fontWeight = FontWeight.Black
        )
        val textLayoutResult = textMeasurer.measure(
            text = "NORMA",
            style = textStyle
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                center.x - textLayoutResult.size.width / 2,
                centerY - textLayoutResult.size.height / 0.95f / 2
            )
        )

        // Draw Registered Trademark symbol
        val rStyle = TextStyle(
            color = Color.White,
            fontSize = (logoHeight * 0.12f).toSp(),
            fontWeight = FontWeight.Bold
        )
        val rLayoutResult = textMeasurer.measure(
            text = "®",
            style = rStyle
        )
        drawText(
            textLayoutResult = rLayoutResult,
            topLeft = Offset(
                center.x + textLayoutResult.size.width / 2 + (logoHeight * 0.02f),
                centerY - textLayoutResult.size.height * 0.48f
            )
        )
    }
}

private fun DrawScope.drawNormaBackground(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    yellow: Color,
    orange: Color,
    red: Color
) {
    val stripeHeight = height * 0.12f
    val curveInset = width * 0.015f

    // Helper to draw a horizontal band with curved sides
    fun drawCurvedBand(yTop: Float, yBottom: Float, color: Color) {
        val path = Path().apply {
            moveTo(left + curveInset, yTop)
            lineTo(left + width - curveInset, yTop)
            quadraticTo(left + width, (yTop + yBottom) / 2, left + width - curveInset, yBottom)
            lineTo(left + curveInset, yBottom)
            quadraticTo(left, (yTop + yBottom) / 2, left + curveInset, yTop)
            close()
        }
        drawPath(path, color)
    }

    // Yellow top
    drawCurvedBand(top, top + stripeHeight, yellow)
    // Orange top
    drawCurvedBand(top + stripeHeight, top + stripeHeight * 2, orange)
    // Red center
    drawCurvedBand(top + stripeHeight * 2, top + height - stripeHeight * 2, red)
    // Orange bottom
    drawCurvedBand(top + height - stripeHeight * 2, top + height - stripeHeight, orange)
    // Yellow bottom
    drawCurvedBand(top + height - stripeHeight, top + height, yellow)
}

@Preview(showBackground = true)
@Composable
private fun NormaLogoPreview() {
    ShopMeTheme {
        NormaLogo(
            modifier = Modifier
                .padding(16.dp)
                .size(300.dp)
        )
    }
}
