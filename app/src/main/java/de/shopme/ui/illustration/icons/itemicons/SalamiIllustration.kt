package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
import kotlin.random.Random

@Composable
fun SalamiIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.salami_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Salami Body
            drawSalamiBody(w, h)

            // 2. Salami Slices
            drawSalamiSlice(Offset(w * 0.78f, h * 0.7f), w * 0.18f, rotation = 5f)
            drawSalamiSlice(Offset(w * 0.86f, h * 0.62f), w * 0.16f, rotation = 15f)

            // 3. Face on the cut surface
            drawShockedFace(Offset(w * 0.58f, h * 0.48f), w * 0.25f)
        }
    }
}

private fun DrawScope.drawSalamiBody(w: Float, h: Float) {
    val bodyPath = Path().apply {
        moveTo(w * 0.15f, h * 0.45f)
        cubicTo(w * 0.2f, h * 0.2f, w * 0.6f, h * 0.25f, w * 0.75f, h * 0.4f)
        lineTo(w * 0.75f, h * 0.65f)
        cubicTo(w * 0.6f, h * 0.75f, w * 0.2f, h * 0.75f, w * 0.15f, h * 0.55f)
        close()
    }

    // Patterned body
    clipPath(bodyPath) {
        drawRect(Color(0xFFD32F2F)) // Base red
        
        val step = w * 0.05f
        for (i in 0..20) {
            for (j in 0..20) {
                if ((i + j) % 2 == 0) {
                    drawRect(
                        color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                        topLeft = Offset(i * step, j * step),
                        size = Size(step, step)
                    )
                }
            }
        }
    }

    // Body outline
    drawPath(bodyPath, BrandBlack, style = Stroke(width = 4f))

    // Strings wrapping around
    val stringColor = Color(0xFFFFF9C4)
    for (i in 1..4) {
        val x = w * (0.15f + i * 0.12f)
        val stringPath = Path().apply {
            moveTo(x, h * 0.32f)
            quadraticTo(x + w * 0.05f, h * 0.45f, x, h * 0.68f)
        }
        drawPath(stringPath, stringColor, style = Stroke(width = 3f))
    }
    
    // Horizontal string
    val horizStringPath = Path().apply {
        moveTo(w * 0.15f, h * 0.5f)
        quadraticTo(w * 0.45f, h * 0.55f, w * 0.75f, h * 0.5f)
    }
    drawPath(horizStringPath, stringColor, style = Stroke(width = 3f))

    // Tied end
    drawTiedEnd(Offset(w * 0.12f, h * 0.5f))

    // Cut surface
    val surfacePath = Path().apply {
        addOval(Rect(center = Offset(w * 0.58f, h * 0.48f), radius = w * 0.25f))
    }
    drawPath(surfacePath, Color(0xFFEF5350))
    drawPath(surfacePath, BrandBlack, style = Stroke(width = 4f))

    // Fat spots on surface
    clipPath(surfacePath) {
        drawFatSpots(Offset(w * 0.58f, h * 0.48f), w * 0.25f)
    }
}

private fun DrawScope.drawSalamiSlice(center: Offset, radius: Float, rotation: Float) {
    rotate(rotation, center) {
        val slicePath = Path().apply {
            addOval(Rect(center = center, radius = radius))
        }
        // Slice side
        translate(2f, 4f) {
            drawPath(slicePath, Color(0xFFB71C1C))
        }
        
        drawPath(slicePath, Color(0xFFEF5350))
        drawPath(slicePath, BrandBlack, style = Stroke(width = 4f))

        clipPath(slicePath) {
            drawFatSpots(center, radius)
        }
    }
}

private fun DrawScope.drawFatSpots(center: Offset, radius: Float) {
    val seed = (center.x * 1000 + center.y).toInt()
    val random = Random(seed)
    for (i in 0..15) {
        val angle = random.nextFloat() * 2 * Math.PI
        val dist = random.nextFloat() * radius * 0.8f
        val spotCenter = Offset(
            center.x + (dist * Math.cos(angle)).toFloat(),
            center.y + (dist * Math.sin(angle)).toFloat()
        )
        val spotRadius = (2f + random.nextFloat() * 6f)
        drawCircle(Color(0xFFFFEBEE).copy(alpha = 0.8f), radius = spotRadius, center = spotCenter)
    }
}

private fun DrawScope.drawShockedFace(center: Offset, radius: Float) {
    // Eyes
    val eyeWidth = radius * 0.4f
    val eyeHeight = radius * 0.5f
    val eyeOffset = radius * 0.25f

    drawEye(Offset(center.x - eyeOffset, center.y - radius * 0.2f), eyeWidth, eyeHeight)
    drawEye(Offset(center.x + eyeOffset, center.y - radius * 0.2f), eyeWidth, eyeHeight)

    // Eyebrows
    val browPathL = Path().apply {
        moveTo(center.x - eyeOffset - eyeWidth * 0.5f, center.y - radius * 0.55f)
        quadraticTo(center.x - eyeOffset, center.y - radius * 0.7f, center.x - eyeOffset + eyeWidth * 0.5f, center.y - radius * 0.55f)
    }
    drawPath(browPathL, BrandBlack, style = Stroke(width = 5f))

    val browPathR = Path().apply {
        moveTo(center.x + eyeOffset - eyeWidth * 0.5f, center.y - radius * 0.55f)
        quadraticTo(center.x + eyeOffset, center.y - radius * 0.7f, center.x + eyeOffset + eyeWidth * 0.5f, center.y - radius * 0.55f)
    }
    drawPath(browPathR, BrandBlack, style = Stroke(width = 5f))

    // Mouth
    val mouthPath = Path().apply {
        moveTo(center.x - radius * 0.3f, center.y + radius * 0.2f)
        cubicTo(
            center.x - radius * 0.4f, center.y + radius * 0.7f,
            center.x + radius * 0.4f, center.y + radius * 0.7f,
            center.x + radius * 0.3f, center.y + radius * 0.2f
        )
        close()
    }
    drawPath(mouthPath, Color(0xFF421010))
    drawPath(mouthPath, BrandBlack, style = Stroke(width = 3f))

    // Tongue
    clipPath(mouthPath) {
        drawCircle(Color(0xFFE57373), radius = radius * 0.25f, center = Offset(center.x, center.y + radius * 0.65f))
    }
}

private fun DrawScope.drawEye(center: Offset, width: Float, height: Float) {
    val eyePath = Path().apply {
        addOval(Rect(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2))
    }
    drawPath(eyePath, BrandWhite)
    drawPath(eyePath, BrandBlack, style = Stroke(width = 3f))

    // Pupil
    drawCircle(BrandBlack, radius = width * 0.3f, center = center.plus(Offset(0f, height * 0.1f)))
    // Highlight
    drawCircle(BrandWhite, radius = width * 0.1f, center = center.plus(Offset(width * 0.1f, -height * 0.1f)))
}

private fun DrawScope.drawTiedEnd(center: Offset) {
    val endPath = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - 20f, center.y - 25f)
        lineTo(center.x - 35f, center.y - 10f)
        lineTo(center.x - 25f, center.y)
        lineTo(center.x - 35f, center.y + 10f)
        lineTo(center.x - 20f, center.y + 25f)
        close()
    }
    drawPath(endPath, Color(0xFFD32F2F))
    drawPath(endPath, BrandBlack, style = Stroke(width = 3f))
    
    // String tie
    drawCircle(Color(0xFFFFF9C4), radius = 6f, center = center)
    drawCircle(BrandBlack, radius = 6f, center = center, style = Stroke(width = 2f))
}

@Preview(showBackground = true)
@Composable
private fun SalamiIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        SalamiIllustration()
    }
}
