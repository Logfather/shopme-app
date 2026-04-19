package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun DmLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.dm_logo_description)
    val dmBlue = Color(0xFF223884)
    val dmYellow = Color(0xFFFFCC00)
    val dmRed = Color(0xFFE4002B)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height

        // Draw the waves (banners)
        drawWaves(width, height, dmYellow, dmRed)

        // Draw the "dm" text
        drawDmText(width, height, dmBlue)
    }
}

private fun DrawScope.drawWaves(width: Float, height: Float, yellow: Color, red: Color) {
    // Red wave (bottom/back)
    val redPath = Path().apply {
        moveTo(width * 0.05f, height * 0.72f)
        cubicTo(width * 0.25f, height * 0.55f, width * 0.45f, height * 0.85f, width * 0.65f, height * 0.65f)
        cubicTo(width * 0.80f, height * 0.50f, width * 0.95f, height * 0.65f, width * 0.95f, height * 0.58f)
        lineTo(width * 0.90f, height * 0.72f)
        cubicTo(width * 0.85f, height * 0.75f, width * 0.70f, height * 0.65f, width * 0.55f, height * 0.85f)
        cubicTo(width * 0.40f, height * 1.00f, width * 0.20f, height * 0.75f, width * 0.05f, height * 0.85f)
        close()
    }
    drawPath(redPath, red)

    // Yellow wave (top/front)
    val yellowPath = Path().apply {
        moveTo(width * 0.08f, height * 0.65f)
        cubicTo(width * 0.30f, height * 0.50f, width * 0.50f, height * 0.80f, width * 0.70f, height * 0.60f)
        cubicTo(width * 0.85f, height * 0.45f, width * 0.98f, height * 0.65f, width * 0.98f, height * 0.65f)
        lineTo(width * 0.95f, height * 0.78f)
        cubicTo(width * 0.80f, height * 0.60f, width * 0.65f, height * 0.88f, width * 0.45f, height * 0.75f)
        cubicTo(width * 0.25f, height * 0.62f, width * 0.15f, height * 0.78f, width * 0.08f, height * 0.75f)
        close()
    }
    drawPath(yellowPath, yellow)
}

private fun DrawScope.drawDmText(width: Float, height: Float, color: Color) {
    // Slanted "dm" text
    val dmPath = Path().apply {
        // 'd'
        // Bowl
        moveTo(width * 0.43f, height * 0.45f)
        cubicTo(width * 0.43f, height * 0.30f, width * 0.23f, height * 0.30f, width * 0.23f, height * 0.45f)
        cubicTo(width * 0.23f, height * 0.60f, width * 0.43f, height * 0.60f, width * 0.43f, height * 0.45f)
        close()
        // Stem
        moveTo(width * 0.50f, height * 0.10f)
        lineTo(width * 0.44f, height * 0.55f)
        lineTo(width * 0.36f, height * 0.55f)
        lineTo(width * 0.42f, height * 0.10f)
        close()

        // 'm'
        // First stem/hump
        moveTo(width * 0.52f, height * 0.35f)
        lineTo(width * 0.49f, height * 0.55f)
        lineTo(width * 0.57f, height * 0.55f)
        cubicTo(width * 0.58f, height * 0.40f, width * 0.63f, height * 0.35f, width * 0.68f, height * 0.35f)
        cubicTo(width * 0.75f, height * 0.35f, width * 0.72f, height * 0.45f, width * 0.70f, height * 0.55f)
        lineTo(width * 0.78f, height * 0.55f)
        cubicTo(width * 0.80f, height * 0.40f, width * 0.83f, height * 0.35f, width * 0.88f, height * 0.35f)
        cubicTo(width * 0.95f, height * 0.35f, width * 0.92f, height * 0.45f, width * 0.90f, height * 0.55f)
        lineTo(width * 0.98f, height * 0.55f)
        lineTo(width * 1.00f, height * 0.32f)
        cubicTo(width * 0.98f, height * 0.25f, width * 0.85f, height * 0.25f, width * 0.78f, height * 0.35f)
        cubicTo(width * 0.75f, height * 0.25f, width * 0.62f, height * 0.25f, width * 0.52f, height * 0.35f)
        close()
    }
    
    drawPath(dmPath, color)
}

@Preview(showBackground = true)
@Composable
private fun DmLogoPreview() {
    DmLogo(
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp)
    )
}
