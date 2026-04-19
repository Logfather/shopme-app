package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import de.shopme.ui.theme.BrandWhite

@OptIn(ExperimentalTextApi::class)
@Composable
fun ReweLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.rewe_logo_description)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = diameter / 2

        // REWE Red Circle Background
        drawCircle(
            color = Color(0xFFE30613),
            radius = radius,
            center = center
        )

        // Draw "REWE" text
        // Using a very bold weight to match the logo's heavy typeface
        val textStyle = TextStyle(
            color = BrandWhite,
            fontSize = (radius * 0.65f).toSp(),
            fontWeight = FontWeight.Black,
            letterSpacing = (radius * -0.02f).toSp()
        )
        val textLayoutResult = textMeasurer.measure(
            text = "REWE",
            style = textStyle
        )

        // Center the text vertically and horizontally
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                center.x - textLayoutResult.size.width / 2f,
                center.y - textLayoutResult.size.height / 2f
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReweLogoPreview() {
    ReweLogo(
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp)
    )
}
