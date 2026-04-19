package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun KauflandLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.kaufland_logo_description)
    val textMeasurer = rememberTextMeasurer()
    val kauflandRed = Color(0xFFE40101)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        
        // Background Circle
        drawCircle(
            color = BrandWhite,
            radius = w / 2,
            center = center
        )
        drawCircle(
            color = Color(0xFFF0F0F0),
            radius = w / 2,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Square Logo part
        val logoSize = w * 0.45f
        val logoTop = center.y - logoSize * 0.75f
        val logoLeft = center.x - logoSize / 2
        
        val strokeWidth = logoSize * 0.08f
        
        // Red Square Frame
        drawRect(
            color = kauflandRed,
            topLeft = Offset(logoLeft, logoTop),
            size = Size(logoSize, logoSize),
            style = Stroke(width = strokeWidth)
        )
        
        // Inner "K" shapes
        val innerPadding = logoSize * 0.18f
        val innerTop = logoTop + innerPadding
        val innerLeft = logoLeft + innerPadding
        val innerWidth = logoSize - 2 * innerPadding
        val innerHeight = logoSize - 2 * innerPadding
        
        val barWidth = innerWidth * 0.32f
        
        // Left Vertical Bar
        drawRect(
            color = kauflandRed,
            topLeft = Offset(innerLeft, innerTop),
            size = Size(barWidth, innerHeight)
        )
        
        // "K" arms (triangles)
        val armLeft = innerLeft + barWidth + innerWidth * 0.1f
        val armWidth = innerWidth - (barWidth + innerWidth * 0.1f)
        
        // Top Triangle
        val topTrianglePath = Path().apply {
            moveTo(armLeft, innerTop)
            lineTo(armLeft + armWidth, innerTop)
            lineTo(armLeft, innerTop + innerHeight / 2)
            close()
        }
        drawPath(topTrianglePath, kauflandRed)
        
        // Bottom Triangle
        val bottomTrianglePath = Path().apply {
            moveTo(armLeft, innerTop + innerHeight / 2)
            lineTo(armLeft + armWidth, innerTop + innerHeight)
            lineTo(armLeft, innerTop + innerHeight)
            close()
        }
        drawPath(bottomTrianglePath, kauflandRed)
        
        // Text "Kaufland"
        val textStyle = TextStyle(
            color = kauflandRed,
            fontSize = (w * 0.18f).toSp(),
            fontWeight = FontWeight.Bold
        )
        val textLayoutResult = textMeasurer.measure(
            text = "Kaufland",
            style = textStyle
        )
        
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                center.x - textLayoutResult.size.width / 2,
                logoTop + logoSize + w * 0.05f
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KauflandLogoPreview() {
    KauflandLogo(
        modifier = Modifier
            .padding(16.dp)
            .size(300.dp)
    )
}
