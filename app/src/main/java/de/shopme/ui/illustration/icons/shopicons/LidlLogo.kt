package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@OptIn(ExperimentalTextApi::class)
@Composable
fun LidlLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.lidl_logo_description)
    val textMeasurer = rememberTextMeasurer()

    val lidlBlue = Color(0xFF213892)
    val lidlRed = Color(0xFFE11A27)
    val lidlYellow = Color(0xFFFFF000)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = diameter / 2
        val borderThickness = diameter * 0.05f

        drawCircle(
            color = lidlYellow,
            radius = radius - borderThickness / 2,
            center = center
        )

        drawCircle(
            color = lidlRed,
            radius = radius - borderThickness / 2,
            center = center,
            style = Stroke(width = borderThickness)
        )

        val fontSize = (diameter * 0.42f).toSp()
        val textStyleBlue = TextStyle(
            color = lidlBlue,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif
        )

        val l1Layout = textMeasurer.measure("L", textStyleBlue)
        val dLayout = textMeasurer.measure("D", textStyleBlue)
        val l2Layout = textMeasurer.measure("L", textStyleBlue)

        val iSquareSize = diameter * 0.28f
        val iDotRadius = diameter * 0.055f
        
        val totalWidth = l1Layout.size.width + iSquareSize + dLayout.size.width + l2Layout.size.width - (diameter * 0.05f)
        var currentX = center.x - totalWidth / 2

        drawText(
            textLayoutResult = l1Layout,
            topLeft = Offset(currentX, center.y - l1Layout.size.height / 2)
        )
        currentX += l1Layout.size.width - (diameter * 0.02f)

        val iCenterX = currentX + iSquareSize / 2
        val iCenterY = center.y + (diameter * 0.03f)
        
        rotate(degrees = 45f, pivot = Offset(iCenterX, iCenterY)) {
            drawRect(
                color = lidlRed,
                topLeft = Offset(iCenterX - iSquareSize / 2.8f, iCenterY - iSquareSize / 2.8f),
                size = Size(iSquareSize / 1.4f, iSquareSize / 1.4f)
            )
        }
        
        drawCircle(
            color = lidlRed,
            radius = iDotRadius,
            center = Offset(iCenterX, iCenterY - (diameter * 0.32f))
        )
        
        currentX += iSquareSize - (diameter * 0.03f)

        drawText(
            textLayoutResult = dLayout,
            topLeft = Offset(currentX, center.y - dLayout.size.height / 2)
        )
        currentX += dLayout.size.width - (diameter * 0.01f)

        drawText(
            textLayoutResult = l2Layout,
            topLeft = Offset(currentX, center.y - l2Layout.size.height / 2)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LidlLogoPreview() {
    ShopMeTheme {
        LidlLogo(
            modifier = Modifier
                .padding(16.dp)
                .size(300.dp)
        )
    }
}
