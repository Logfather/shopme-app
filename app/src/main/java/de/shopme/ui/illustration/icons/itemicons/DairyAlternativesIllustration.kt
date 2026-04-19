package de.shopme.ui.illustration.icons.itemicons
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
@Composable
fun DairyAlternativesIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.dairy_alternatives_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Background Decorations: Splashes and Leaves
        BackgroundDecorations(modifier = Modifier.fillMaxSize())
        // Almond Milk Carton (Back Left)
        AlmondMilkCarton(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 30.dp)
        )
        // Oat Milk Bottle (Back Right)
        OatMilkBottle(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 40.dp)
        )
        // Grain Bag Character (Bottom Right)
        GrainBagCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-50).dp)
        )
        // Coconut Character (Bottom Center)
        CoconutCharacter(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        )
        // Almond Characters (Bottom Left)
        AlmondCharacters(
            modifier = Modifier
                .fillMaxSize(0.3f)
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-40).dp)
        )
        // Foreground elements: scattered nuts and olives
        ForegroundDecorations(modifier = Modifier.fillMaxSize())
    }
}
@Composable
private fun AlmondMilkCarton(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Carton Body
            val cartonPath = Path().apply {
                moveTo(w * 0.15f, h * 0.3f)
                lineTo(w * 0.85f, h * 0.25f)
                lineTo(w * 0.9f, h * 0.95f)
                lineTo(w * 0.1f, h * 0.98f)
                close()
            }
            drawPath(
                path = cartonPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81D4FA), Color(0xFF29B6F6))
                )
            )
            drawPath(cartonPath, BrandBlack, style = Stroke(width = 3f))
            // Carton Top (Folded part)
            val topPath = Path().apply {
                moveTo(w * 0.15f, h * 0.3f)
                lineTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.85f, h * 0.25f)
            }
            drawPath(topPath, Color(0xFFB3E5FC))
            drawPath(topPath, BrandBlack, style = Stroke(width = 3f))
            // Cap
            drawCircle(BrandWhite, radius = 15f, center = Offset(w * 0.65f, h * 0.15f))
            drawCircle(BrandBlack, radius = 15f, center = Offset(w * 0.65f, h * 0.15f), style = Stroke(width = 2f))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.almond_milk_label),
                color = Color(0xFF0277BD),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Almond shapes on carton
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawAlmondShape(size.width, size.height, Color(0xFF8D6E63))
                    }
                }
            }
        }
    }
}
@Composable
private fun OatMilkBottle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bottle Shape
            val bottlePath = Path().apply {
                moveTo(w * 0.35f, h * 0.2f)
                quadraticTo(w * 0.35f, h * 0.1f, w * 0.5f, h * 0.1f)
                quadraticTo(w * 0.65f, h * 0.1f, w * 0.65f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.4f)
                quadraticTo(w * 0.85f, h * 0.5f, w * 0.85f, h * 0.85f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.15f, h * 0.85f)
                quadraticTo(w * 0.15f, h * 0.5f, w * 0.25f, h * 0.4f)
                close()
            }
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
                )
            )
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 3f))
            // Cork/Cap
            val capPath = Path().apply {
                moveTo(w * 0.33f, h * 0.05f)
                lineTo(w * 0.67f, h * 0.05f)
                lineTo(w * 0.65f, h * 0.15f)
                lineTo(w * 0.35f, h * 0.15f)
                close()
            }
            drawPath(capPath, Color(0xFF8D6E63))
            drawPath(capPath, BrandBlack, style = Stroke(width = 3f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CharacterFace(
                modifier = Modifier.offset(y = (-10).dp),
                eyeSize = 14.dp,
                mouthSize = 20.dp
            )
            Text(
                text = stringResource(R.string.oat_milk_label),
                color = Color(0xFF5D4037),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
@Composable
private fun CoconutCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Outer Brown Shell
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6D4C41), Color(0xFF3E2723)),
                    center = center,
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = center
            )
            drawCircle(BrandBlack, radius = w * 0.45f, center = center, style = Stroke(width = 3f))

            // White Inner
            drawCircle(BrandWhite, radius = w * 0.35f, center = center)
            drawCircle(BrandBlack, radius = w * 0.35f, center = center, style = Stroke(width = 2f))

            // Texture lines on shell
            for (i in 0..5) {
                rotate(i * 60f) {
                    drawLine(Color(0xFF4E342E), Offset(w * 0.85f, h * 0.5f), Offset(w * 0.95f, h * 0.5f), strokeWidth = 2f)
                }
            }
        }
    }
}
@Composable
private fun GrainBagCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bag Shape
            val bagPath = Path().apply {
                moveTo(w * 0.1f, h * 0.4f)
                quadraticTo(w * 0.1f, h * 0.3f, w * 0.5f, h * 0.3f)
                quadraticTo(w * 0.9f, h * 0.3f, w * 0.9f, h * 0.4f)
                lineTo(w * 0.95f, h * 0.85f)
                quadraticTo(w * 0.95f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.05f, h * 0.95f, w * 0.05f, h * 0.85f)
                close()
            }
            drawPath(path = bagPath, color = Color(0xFFFBC02D))
            drawPath(bagPath, BrandBlack, style = Stroke(width = 3f))

            // Bag top rim
            val rimPath = Path().apply {
                moveTo(w * 0.1f, h * 0.4f)
                quadraticTo(w * 0.5f, h * 0.35f, w * 0.9f, h * 0.4f)
                lineTo(w * 0.9f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.45f, w * 0.1f, h * 0.5f)
                close()
            }
            drawPath(path = rimPath, color = Color(0xFFF9A825))
            drawPath(rimPath, BrandBlack, style = Stroke(width = 2f))
            // Grains inside
            for (i in 0..15) {
                drawCircle(Color(0xFFFFF59D), radius = 6f, center = Offset(w * (0.2f + (i % 5) * 0.15f), h * (0.35f + (i / 5) * 0.05f)))
            }
        }
        CharacterFace(
            modifier = Modifier.offset(y = 15.dp),
            eyeSize = 12.dp,
            mouthSize = 18.dp
        )
    }
}
@Composable
private fun AlmondCharacters(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AlmondCharacter(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterStart)
        )
        AlmondCharacter(
            modifier = Modifier
                .size(55.dp)
                .align(Alignment.CenterEnd)
                .offset(y = 10.dp)
        )
    }
}
@Composable
private fun AlmondCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAlmondShape(size.width, size.height, Color(0xFF795548))
            drawAlmondShape(size.width, size.height, BrandBlack, style = Stroke(width = 3f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f),
            eyeSize = 8.dp,
            mouthSize = 12.dp
        )
    }
}
private fun DrawScope.drawAlmondShape(w: Float, h: Float, color: Color, style: androidx.compose.ui.graphics.drawscope.DrawStyle = androidx.compose.ui.graphics.drawscope.Fill) {
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.1f)
        quadraticTo(w * 0.9f, h * 0.4f, w * 0.8f, h * 0.8f)
        quadraticTo(w * 0.5f, h * 0.95f, w * 0.2f, h * 0.8f)
        quadraticTo(w * 0.1f, h * 0.4f, w * 0.5f, h * 0.1f)
        close()
    }
    drawPath(path, color, style = style)
}
@Composable
private fun BackgroundDecorations(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Milk Splashes
        drawSplash(Offset(size.width * 0.2f, size.height * 0.5f), 100f)
        drawSplash(Offset(size.width * 0.5f, size.height * 0.8f), 120f)
        drawSplash(Offset(size.width * 0.8f, size.height * 0.3f), 80f)

        // Leaves
        drawLeaf(Offset(size.width * 0.1f, size.height * 0.3f), 40f, 45f)
        drawLeaf(Offset(size.width * 0.9f, size.height * 0.4f), 50f, -30f)
        drawLeaf(Offset(size.width * 0.15f, size.height * 0.85f), 45f, 160f)

        // Droplets
        drawCircle(Color(0xFFFFD54F), radius = 8f, center = Offset(size.width * 0.45f, size.height * 0.1f))
        drawCircle(Color(0xFF81D4FA), radius = 6f, center = Offset(size.width * 0.05f, size.height * 0.4f))
    }
}
@Composable
private fun ForegroundDecorations(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Olives
        drawOlive(Offset(size.width * 0.7f, size.height * 0.85f), 20f)
        drawOlive(Offset(size.width * 0.8f, size.height * 0.8f), 18f)
        drawOlive(Offset(size.width * 0.65f, size.height * 0.92f), 22f)

        // Small nuts
        drawCircle(Color(0xFFD2B48C), radius = 5f, center = Offset(size.width * 0.4f, size.height * 0.95f))
        drawCircle(Color(0xFFD2B48C), radius = 6f, center = Offset(size.width * 0.55f, size.height * 0.93f))
    }
}
private fun DrawScope.drawSplash(center: Offset, radius: Float) {
    val path = Path().apply {
        for (i in 0..7) {
            val angle = i * 45f * (Math.PI / 180).toFloat()
            val r = if (i % 2 == 0) radius else radius * 0.5f
            val x = center.x + r * Math.cos(angle.toDouble()).toFloat()
            val y = center.y + r * Math.sin(angle.toDouble()).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    // Smooth out the splash
    drawPath(path, BrandWhite.copy(alpha = 0.8f))
}
private fun DrawScope.drawLeaf(center: Offset, size: Float, rotationDegrees: Float) {
    rotate(rotationDegrees, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            quadraticTo(center.x + size, center.y, center.x, center.y + size)
            quadraticTo(center.x - size, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, Color(0xFF4CAF50))
        drawPath(path, BrandBlack, style = Stroke(width = 2f))
        drawLine(BrandBlack, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 1f)
    }
}
private fun DrawScope.drawOlive(center: Offset, radius: Float) {
    drawOval(Color(0xFF8BC34A), topLeft = Offset(center.x - radius, center.y - radius * 0.7f), size = Size(radius * 2, radius * 1.4f))
    drawOval(BrandBlack, topLeft = Offset(center.x - radius, center.y - radius * 0.7f), size = Size(radius * 2, radius * 1.4f), style = Stroke(width = 2f))
}
@Composable
private fun CharacterFace(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 16.dp,
    mouthSize: Dp = 20.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CharacterEye(eyeSize = eyeSize)
            CharacterEye(eyeSize = eyeSize)
        }
        Spacer(modifier = Modifier.height(2.dp))
        OpenMouth(modifier = Modifier.size(mouthSize))
    }
}
@Composable
private fun CharacterEye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(BrandWhite, CircleShape)
            .border(1.5.dp, BrandBlack, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBlack,
                radius = size.minDimension * 0.35f,
                center = center.copy(y = center.y + size.height * 0.05f)
            )
            drawCircle(
                color = BrandWhite,
                radius = size.minDimension * 0.15f,
                center = center.copy(x = center.x + size.width * 0.2f, y = center.y - size.height * 0.15f)
            )
        }
    }
}
@Composable
private fun OpenMouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height * 1.2f, size.width, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height * 0.1f, 0f, size.height * 0.2f)
            close()
        }
        drawPath(path, Color(0xFFB71C1C))
        drawPath(path, BrandBlack, style = Stroke(width = 2f))
    }
}
@Preview(showBackground = true)
@Composable
private fun DairyAlternativesIllustrationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        DairyAlternativesIllustration(modifier = Modifier.size(400.dp))
    }
}
