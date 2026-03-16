package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
fun AldiLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.aldi_logo_description)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = diameter / 2

        // Outer Borders
        // Red
        drawCircle(
            color = Color(0xFFE4002B),
            radius = radius,
            center = center
        )
        // Orange
        drawCircle(
            color = Color(0xFFF39200),
            radius = radius * 0.94f,
            center = center
        )
        // Yellow
        drawCircle(
            color = Color(0xFFFFC20E),
            radius = radius * 0.88f,
            center = center
        )
        // Dark Blue Background
        drawCircle(
            color = Color(0xFF00285E),
            radius = radius * 0.82f,
            center = center
        )

        // Draw the 3 blue stripes
        drawAldiStripes(center, radius)

        // Draw "ALDI" text
        val textStyle = TextStyle(
            color = Color.White,
            fontSize = (radius * 0.38f).toSp(),
            fontWeight = FontWeight.Black
        )
        val textLayoutResult = textMeasurer.measure(
            text = "ALDI",
            style = textStyle
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                center.x - textLayoutResult.size.width / 2,
                center.y + radius * 0.18f
            )
        )
    }
}

private fun DrawScope.drawAldiStripes(center: Offset, radius: Float) {
    val stripeColorTop = Color(0xFF82D1F1)
    val stripeColorBottom = Color(0xFF00A1DE)

    val stripeWidth = radius * 0.16f
    val stripeGap = radius * 0.05f

    // Slant factor - stripes are slanted to the right
    val slantX = radius * 0.22f

    val startX = center.x - radius * 0.38f
    val startY = center.y - radius * 0.58f

    val brush = Brush.verticalGradient(
        colors = listOf(stripeColorTop, stripeColorBottom),
        startY = startY,
        endY = startY + radius * 0.65f
    )

    // Stripe 1 (Leftmost, longest)
    val path1 = Path().apply {
        moveTo(startX + slantX, startY)
        lineTo(startX + slantX + stripeWidth, startY)
        lineTo(startX + stripeWidth, startY + radius * 0.65f)
        lineTo(startX, startY + radius * 0.65f)
        close()
    }

    // Stripe 2 (Middle)
    val startX2 = startX + stripeWidth + stripeGap
    val startY2 = startY + radius * 0.12f
    val path2 = Path().apply {
        moveTo(startX2 + slantX * 0.78f, startY2)
        lineTo(startX2 + slantX * 0.78f + stripeWidth, startY2)
        lineTo(startX2 + stripeWidth, startY2 + radius * 0.53f)
        lineTo(startX2, startY2 + radius * 0.53f)
        close()
    }

    // Stripe 3 (Rightmost, shortest)
    val startX3 = startX2 + stripeWidth + stripeGap
    val startY3 = startY + radius * 0.28f
    val path3 = Path().apply {
        moveTo(startX3 + slantX * 0.52f, startY3)
        lineTo(startX3 + slantX * 0.52f + stripeWidth, startY3)
        lineTo(startX3 + stripeWidth, startY3 + radius * 0.37f)
        lineTo(startX3, startY3 + radius * 0.37f)
        close()
    }

    drawPath(path1, brush)
    drawPath(path2, brush)
    drawPath(path3, brush)
}

@Preview(showBackground = true)
@Composable
private fun AldiLogoPreview() {
    ShopMeTheme {
        AldiLogo(
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp)
        )
    }
}
