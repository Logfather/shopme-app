package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun RossmannLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.rossmann_logo_description)
    val rossmannRed = Color(0xFFCE1126)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val ringStrokeWidth = diameter * 0.09f

        drawCircle(
            color = rossmannRed,
            radius = radius - ringStrokeWidth / 2f,
            center = center,
            style = Stroke(width = ringStrokeWidth)
        )

        val path = Path().apply {
            val barY = center.y - diameter * 0.16f
            val barHeight = diameter * 0.07f
            val barWidth = diameter * 0.78f
            
            addRect(
                Rect(
                    center.x - barWidth / 2f,
                    barY,
                    center.x + barWidth / 2f,
                    barY + barHeight
                )
            )

            moveTo(center.x + diameter * 0.08f, barY)
            lineTo(center.x + diameter * 0.11f, barY - diameter * 0.16f)
            lineTo(center.x + diameter * 0.19f, barY - diameter * 0.16f)
            lineTo(center.x + diameter * 0.16f, barY + diameter * 0.05f)
            close()

            moveTo(center.x + diameter * 0.14f, barY + barHeight)
            lineTo(center.x - diameter * 0.38f, barY + diameter * 0.15f)
            lineTo(center.x - diameter * 0.38f, barY + diameter * 0.28f)
            lineTo(center.x + diameter * 0.08f, barY + diameter * 0.25f)
            close()
            
            moveTo(center.x - diameter * 0.38f, barY + diameter * 0.18f)
            lineTo(center.x - diameter * 0.44f, barY + diameter * 0.38f)
            lineTo(center.x - diameter * 0.36f, barY + diameter * 0.38f)
            lineTo(center.x - diameter * 0.32f, barY + diameter * 0.22f)
            close()

            moveTo(center.x - diameter * 0.3f, barY + diameter * 0.26f)
            lineTo(center.x - diameter * 0.18f, barY + diameter * 0.58f)
            lineTo(center.x - diameter * 0.1f, barY + diameter * 0.58f)
            lineTo(center.x - diameter * 0.22f, barY + diameter * 0.26f)
            close()

            moveTo(center.x - diameter * 0.16f, barY + diameter * 0.26f)
            lineTo(center.x + diameter * 0.01f, barY + diameter * 0.58f)
            lineTo(center.x + diameter * 0.09f, barY + diameter * 0.58f)
            lineTo(center.x - diameter * 0.08f, barY + diameter * 0.26f)
            close()

            moveTo(center.x + diameter * 0.01f, barY + diameter * 0.26f)
            lineTo(center.x + diameter * 0.21f, barY + diameter * 0.58f)
            lineTo(center.x + diameter * 0.29f, barY + diameter * 0.58f)
            lineTo(center.x + diameter * 0.09f, barY + diameter * 0.26f)
            close()

            moveTo(center.x + diameter * 0.08f, barY + diameter * 0.26f)
            lineTo(center.x + diameter * 0.26f, barY + diameter * 0.26f)
            lineTo(center.x + diameter * 0.44f, barY + diameter * 0.12f)
            lineTo(center.x + diameter * 0.38f, barY - diameter * 0.02f)
            lineTo(center.x + diameter * 0.24f, barY + diameter * 0.15f)
            close()
        }

        drawPath(path = path, color = rossmannRed)
    }
}

@Preview(showBackground = true)
@Composable
private fun RossmannLogoPreview() {
    RossmannLogo(
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp)
    )
}
