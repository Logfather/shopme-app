package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R

@OptIn(ExperimentalTextApi::class)
@Composable
fun NettoLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.netto_logo_description)
    val textMeasurer = rememberTextMeasurer()
    val nettoRed = Color(0xFFE4002B)
    val nettoYellow = Color(0xFFFFE100)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = diameter / 2

        // Yellow background circle
        drawCircle(
            color = nettoYellow,
            radius = radius,
            center = center
        )

        // "Netto" text
        val nettoTextStyle = TextStyle(
            fontSize = (radius * 0.65f).sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic
        )
        
        val nettoLayoutResult = textMeasurer.measure(
            text = "Netto",
            style = nettoTextStyle
        )

        val nettoTopLeft = Offset(
            center.x - nettoLayoutResult.size.width / 2,
            center.y - nettoLayoutResult.size.height / 0.85f + (radius * 0.4f)
        )

        // Draw the red text
        drawText(
            textLayoutResult = nettoLayoutResult,
            color = nettoRed,
            topLeft = nettoTopLeft
        )

        // Draw the red outline to make it look bolder and match the logo style
        drawText(
            textLayoutResult = nettoLayoutResult,
            color = nettoRed,
            topLeft = nettoTopLeft,
            drawStyle = Stroke(width = radius * 0.05f)
        )

        // "Marken-Discount" text
        val discountTextStyle = TextStyle(
            fontSize = (radius * 0.16f).sp,
            fontWeight = FontWeight.Bold
        )
        
        val discountLayoutResult = textMeasurer.measure(
            text = "Marken-Discount",
            style = discountTextStyle
        )

        drawText(
            textLayoutResult = discountLayoutResult,
            color = nettoRed,
            topLeft = Offset(
                center.x - discountLayoutResult.size.width / 2 + (radius * 0.1f),
                nettoTopLeft.y + nettoLayoutResult.size.height * 0.8f
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NettoLogoPreview() {
    NettoLogo(
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp)
    )
}
