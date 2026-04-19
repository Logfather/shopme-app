package de.shopme.ui.illustration.icons.itemicons
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
fun VeggieIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.veggie_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Background elements (Leaves and shapes)
        VeggieBackground(modifier = Modifier.fillMaxSize())
        // Characters in the back
        VeggieCarrotCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.TopStart)
                .offset(x = 50.dp, y = 20.dp)
                .rotate(-10f)
        )
        VeggieMilkBottle(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.TopCenter)
                .offset(x = (-20).dp, y = 30.dp)
        )
        VeggieBroccoliCharacter(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.TopEnd)
                .offset(x = (-50).dp, y = 10.dp)
        )
        // Characters in the middle/front
        VeggieTofuCharacter(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = (-20).dp)
        )
        VeggieTomatoCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 20.dp)
        )
        VeggieBurgerCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-80).dp)
        )
        // Central Checkmark
        CheckmarkCircle(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )
        // Wooden Ribbon
        VeggieRibbon(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-40).dp)
        )

        // Foreground leaves
        ForegroundVeggieLeaves(modifier = Modifier.fillMaxSize())
    }
}
@Composable
private fun VeggieBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Back green leaves
        drawSimpleVeggieLeaf(Offset(w * 0.2f, h * 0.6f), 50f, 45f, Color(0xFF4CAF50))
        drawSimpleVeggieLeaf(Offset(w * 0.15f, h * 0.55f), 60f, 15f, Color(0xFF388E3C))
    }
}
@Composable
private fun ForegroundVeggieLeaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bottom leaves
        drawSimpleVeggieLeaf(Offset(w * 0.4f, h * 0.88f), 45f, -10f, Color(0xFF689F38))
        drawSimpleVeggieLeaf(Offset(w * 0.5f, h * 0.92f), 55f, 0f, Color(0xFF8BC34A))
        drawSimpleVeggieLeaf(Offset(w * 0.6f, h * 0.88f), 45f, 10f, Color(0xFF689F38))
    }
}
private fun DrawScope.drawSimpleVeggieLeaf(center: Offset, size: Float, rotation: Float, color: Color) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            quadraticTo(center.x + size * 0.7f, center.y, center.x, center.y + size)
            quadraticTo(center.x - size * 0.7f, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, color)
        drawPath(path, Color(0xFF1B5E20), style = Stroke(width = 3f))
        drawLine(Color(0xFF1B5E20), Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 2f)
    }
}
@Composable
private fun CheckmarkCircle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8f
            // Darker green outer ring
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = size.minDimension / 2f
            )
            // Green filled circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)),
                    center = center,
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension * 0.45f
            )
            // White highlight ring (partial)
            drawArc(
                color = BrandWhite.copy(alpha = 0.3f),
                startAngle = -120f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                size = Size(size.width * 0.9f, size.height * 0.9f)
            )
            // Checkmark
            val checkPath = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.75f)
                lineTo(size.width * 0.8f, size.height * 0.3f)
            }
            drawPath(
                path = checkPath,
                color = BrandWhite,
                style = Stroke(width = 24f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )
        }
    }
}
@Composable
private fun VeggieCarrotCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Carrot Body
            val carrotPath = Path().apply {
                moveTo(w * 0.2f, h * 0.2f)
                quadraticTo(w * 0.5f, h * 0.1f, w * 0.8f, h * 0.2f)
                quadraticTo(w * 0.9f, h * 0.6f, w * 0.6f, h * 0.95f)
                quadraticTo(w * 0.5f, h * 1.0f, w * 0.4f, h * 0.95f)
                quadraticTo(w * 0.1f, h * 0.6f, w * 0.2f, h * 0.2f)
            }
            drawPath(
                path = carrotPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
                )
            )
            drawPath(carrotPath, BrandBlack, style = Stroke(width = 4f))
            // Texture lines
            drawLine(Color(0xFFE65100).copy(alpha = 0.6f), Offset(w * 0.3f, h * 0.4f), Offset(w * 0.5f, h * 0.42f), strokeWidth = 3f)
            drawLine(Color(0xFFE65100).copy(alpha = 0.6f), Offset(w * 0.6f, h * 0.6f), Offset(w * 0.8f, h * 0.58f), strokeWidth = 3f)
            // Leaves (simplified)
            drawVeggieCarrotLeaves(w, h)
        }
        Column(
            modifier = Modifier.offset(y = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VeggieCharacterEye(eyeSize = 20.dp)
                VeggieCharacterEye(eyeSize = 20.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            VeggieOpenMouth(modifier = Modifier.size(24.dp, 12.dp))
        }
    }
}
private fun DrawScope.drawVeggieCarrotLeaves(w: Float, h: Float) {
    val greenColor = Color(0xFF689F38)
    val darkGreenColor = Color(0xFF33691E)

    fun leaf(offset: Offset) {
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            cubicTo(w * (0.5f + offset.x), h * (0.1f + offset.y), w * (0.5f + offset.x * 2f), h * (-0.1f + offset.y), w * (0.5f + offset.x), h * (-0.2f + offset.y))
            cubicTo(w * (0.5f - offset.x), h * (-0.1f + offset.y), w * 0.5f, h * 0.1f, w * 0.5f, h * 0.2f)
        }
        drawPath(path, greenColor)
        drawPath(path, darkGreenColor, style = Stroke(width = 3f))
    }

    leaf(Offset(0.1f, 0f))
    leaf(Offset(-0.1f, 0.05f))
    leaf(Offset(0.05f, -0.05f))
}
@Composable
private fun VeggieBroccoliCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Stem
            val stemPath = Path().apply {
                moveTo(w * 0.4f, h * 0.5f)
                lineTo(w * 0.45f, h * 0.9f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.55f, h * 0.9f)
                lineTo(w * 0.6f, h * 0.5f)
            }
            drawPath(stemPath, Color(0xFFAED581))
            drawPath(stemPath, BrandBlack, style = Stroke(width = 4f))
            // Fluffy top
            val topColor = Color(0xFF388E3C)
            fun fluffy(center: Offset, radius: Float) {
                drawCircle(topColor, radius = radius, center = center)
                drawCircle(BrandBlack, radius = radius, center = center, style = Stroke(width = 3f))
            }

            fluffy(Offset(w * 0.3f, h * 0.35f), w * 0.2f)
            fluffy(Offset(w * 0.5f, h * 0.25f), w * 0.25f)
            fluffy(Offset(w * 0.7f, h * 0.35f), w * 0.2f)
            fluffy(Offset(w * 0.5f, h * 0.45f), w * 0.2f)

            // Texture dots on top
            drawCircle(Color(0xFF1B5E20), radius = 5f, center = Offset(w * 0.45f, h * 0.25f))
            drawCircle(Color(0xFF1B5E20), radius = 4f, center = Offset(w * 0.6f, h * 0.3f))
        }
        Column(
            modifier = Modifier.offset(y = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VeggieCharacterEye(eyeSize = 22.dp)
                VeggieCharacterEye(eyeSize = 22.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            VeggieOpenMouth(modifier = Modifier.size(26.dp, 14.dp))
        }
    }
}
@Composable
private fun VeggieMilkBottle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Bottle Body
            val bottlePath = Path().apply {
                moveTo(w * 0.35f, h * 0.2f)
                quadraticTo(w * 0.35f, h * 0.12f, w * 0.5f, h * 0.12f)
                quadraticTo(w * 0.65f, h * 0.12f, w * 0.65f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.35f)
                quadraticTo(w * 0.85f, h * 0.45f, w * 0.85f, h * 0.85f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.15f, h * 0.85f)
                quadraticTo(w * 0.15f, h * 0.45f, w * 0.25f, h * 0.35f)
                close()
            }
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(BrandWhite, Color(0xFFF1F8E9))
                )
            )
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 4f))
            // Green Label
            val labelPath = Path().apply {
                moveTo(w * 0.15f, h * 0.5f)
                lineTo(w * 0.85f, h * 0.5f)
                lineTo(w * 0.85f, h * 0.8f)
                lineTo(w * 0.15f, h * 0.8f)
                close()
            }
            drawPath(labelPath, Color(0xFF4CAF50))
            drawPath(labelPath, BrandBlack, style = Stroke(width = 3f))
            // White leaf on label
            drawCircle(BrandWhite, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.65f))

            // Cap
            drawRoundRect(
                color = Color(0xFFAED581),
                topLeft = Offset(w * 0.33f, h * 0.05f),
                size = Size(w * 0.34f, h * 0.1f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.33f, h * 0.05f),
                size = Size(w * 0.34f, h * 0.1f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 4f)
            )
        }
    }
}
@Composable
private fun VeggieTofuCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Perspective Cube
            val frontPath = Path().apply {
                addRoundRect(RoundRect(Rect(w * 0.1f, h * 0.35f, w * 0.75f, h * 0.9f), CornerRadius(15f, 15f)))
            }
            drawPath(frontPath, Color(0xFFFFF9C4))
            drawPath(frontPath, BrandBlack, style = Stroke(width = 4f))
            val topPath = Path().apply {
                moveTo(w * 0.1f, h * 0.35f)
                lineTo(w * 0.3f, h * 0.2f)
                lineTo(w * 0.95f, h * 0.25f)
                lineTo(w * 0.75f, h * 0.4f)
                close()
            }
            drawPath(topPath, Color(0xFFFFFDE7))
            drawPath(topPath, BrandBlack, style = Stroke(width = 4f))
            val sidePath = Path().apply {
                moveTo(w * 0.75f, h * 0.4f)
                lineTo(w * 0.95f, h * 0.25f)
                lineTo(w * 0.95f, h * 0.75f)
                lineTo(w * 0.75f, h * 0.9f)
                close()
            }
            drawPath(sidePath, Color(0xFFFBC02D))
            drawPath(sidePath, BrandBlack, style = Stroke(width = 4f))

            // Texture holes
            drawCircle(Color(0xFFF9A825).copy(alpha = 0.4f), radius = 6f, center = Offset(w * 0.2f, h * 0.5f))
            drawCircle(Color(0xFFF9A825).copy(alpha = 0.4f), radius = 4f, center = Offset(w * 0.15f, h * 0.7f))
        }
        Column(
            modifier = Modifier.offset(x = (-5).dp, y = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VeggieCharacterEye(eyeSize = 22.dp)
                VeggieCharacterEye(eyeSize = 22.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            VeggieOpenMouth(modifier = Modifier.size(28.dp, 16.dp))
        }
    }
}
@Composable
private fun VeggieTomatoCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Tomato Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
                ),
                radius = w * 0.45f,
                center = center
            )
            drawCircle(BrandBlack, radius = w * 0.45f, center = center, style = Stroke(width = 4f))
            // Stem
            val stemPath = Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.45f, h * 0.05f)
                lineTo(w * 0.55f, h * 0.05f)
                close()
            }
            drawPath(stemPath, Color(0xFF388E3C))
            drawPath(stemPath, BrandBlack, style = Stroke(width = 3f))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VeggieCharacterEye(eyeSize = 18.dp)
                VeggieCharacterEye(eyeSize = 18.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            VeggieOpenMouth(modifier = Modifier.size(24.dp, 12.dp))
        }
    }
}
@Composable
private fun VeggieBurgerCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Bottom Bun
            drawVeggieBun(w, h * 0.75f, w * 0.4f, h * 0.15f, true)

            // Patty
            drawRoundRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(w * 0.15f, h * 0.6f),
                size = Size(w * 0.7f, h * 0.15f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.15f, h * 0.6f),
                size = Size(w * 0.7f, h * 0.15f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 3f)
            )
            // Cheese
            val cheesePath = Path().apply {
                moveTo(w * 0.15f, h * 0.58f)
                lineTo(w * 0.85f, h * 0.58f)
                lineTo(w * 0.7f, h * 0.68f)
                lineTo(w * 0.25f, h * 0.65f)
                close()
            }
            drawPath(cheesePath, Color(0xFFFFD54F))
            drawPath(cheesePath, BrandBlack, style = Stroke(width = 3f))
            // Lettuce (squiggly)
            val lettucePath = Path().apply {
                moveTo(w * 0.1f, h * 0.55f)
                quadraticTo(w * 0.2f, h * 0.5f, w * 0.3f, h * 0.55f)
                quadraticTo(w * 0.4f, h * 0.5f, w * 0.5f, h * 0.55f)
                quadraticTo(w * 0.6f, h * 0.5f, w * 0.7f, h * 0.55f)
                quadraticTo(w * 0.8f, h * 0.5f, w * 0.9f, h * 0.55f)
                lineTo(w * 0.9f, h * 0.6f)
                lineTo(w * 0.1f, h * 0.6f)
                close()
            }
            drawPath(lettucePath, Color(0xFF8BC34A))
            drawPath(lettucePath, BrandBlack, style = Stroke(width = 3f))
            // Tomato slice
            drawRoundRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(w * 0.2f, h * 0.5f),
                size = Size(w * 0.6f, h * 0.08f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.2f, h * 0.5f),
                size = Size(w * 0.6f, h * 0.08f),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 3f)
            )
            // Top Bun
            drawVeggieBun(w, h * 0.3f, w * 0.45f, h * 0.25f, false)

            // Seeds on top bun
            drawCircle(Color(0xFFFFF9C4), radius = 3f, center = Offset(w * 0.4f, h * 0.35f))
            drawCircle(Color(0xFFFFF9C4), radius = 3f, center = Offset(w * 0.6f, h * 0.4f))
        }
        Column(
            modifier = Modifier.offset(y = (-15).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VeggieCharacterEye(eyeSize = 16.dp)
                VeggieCharacterEye(eyeSize = 16.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            VeggieOpenMouth(modifier = Modifier.size(20.dp, 10.dp))
        }
    }
}
private fun DrawScope.drawVeggieBun(w: Float, y: Float, rw: Float, rh: Float, bottom: Boolean) {
    val bunPath = Path().apply {
        if (bottom) {
            moveTo(w * 0.5f - rw, y)
            lineTo(w * 0.5f + rw, y)
            quadraticTo(w * 0.5f + rw, y + rh, w * 0.5f, y + rh)
            quadraticTo(w * 0.5f - rw, y + rh, w * 0.5f - rw, y)
        } else {
            moveTo(w * 0.5f - rw, y + rh)
            lineTo(w * 0.5f + rw, y + rh)
            quadraticTo(w * 0.5f + rw, y, w * 0.5f, y)
            quadraticTo(w * 0.5f - rw, y, w * 0.5f - rw, y + rh)
        }
        close()
    }
    drawPath(bunPath, Color(0xFFFFA726))
    drawPath(bunPath, BrandBlack, style = Stroke(width = 3f))
}
@Composable
private fun VeggieRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Ribbon main (wood texture color)
            val ribbonPath = Path().apply {
                moveTo(w * 0.1f, h * 0.3f)
                quadraticTo(w * 0.5f, h * 0.4f, w * 0.9f, h * 0.3f)
                lineTo(w * 0.92f, h * 0.85f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.08f, h * 0.85f)
                close()
            }
            drawPath(path = ribbonPath, color = Color(0xFFB07F43))
            drawPath(path = ribbonPath, color = BrandBlack, style = Stroke(width = 5f))
            // Wood grain lines
            drawLine(Color(0xFF8D6E63).copy(alpha = 0.5f), Offset(w * 0.2f, h * 0.5f), Offset(w * 0.4f, h * 0.55f), strokeWidth = 4f)
            drawLine(Color(0xFF8D6E63).copy(alpha = 0.5f), Offset(w * 0.6f, h * 0.7f), Offset(w * 0.8f, h * 0.65f), strokeWidth = 4f)
            // Side folds
            val leftFold = Path().apply {
                moveTo(w * 0.1f, h * 0.4f)
                lineTo(w * 0.02f, h * 0.35f)
                lineTo(w * 0.05f, h * 0.6f)
                lineTo(0f, h * 0.8f)
                lineTo(w * 0.1f, h * 0.75f)
                close()
            }
            drawPath(path = leftFold, color = Color(0xFF8D6E63))
            drawPath(path = leftFold, color = BrandBlack, style = Stroke(width = 4f))
            val rightFold = Path().apply {
                moveTo(w * 0.9f, h * 0.4f)
                lineTo(w * 0.98f, h * 0.35f)
                lineTo(w * 0.95f, h * 0.6f)
                lineTo(w, h * 0.8f)
                lineTo(w * 0.9f, h * 0.75f)
                close()
            }
            drawPath(path = rightFold, color = Color(0xFF8D6E63))
            drawPath(path = rightFold, color = BrandBlack, style = Stroke(width = 4f))
        }
        Text(
            text = stringResource(R.string.veggie_label),
            color = Color(0xFFFFFDE7),
            fontSize = 58.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier
                .offset(y = 12.dp)
                .graphicsLayer(scaleY = 1.1f)
        )
    }
}
@Composable
private fun VeggieCharacterEye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(BrandWhite, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBlack,
                radius = size.minDimension * 0.35f,
                center = center
            )
            drawCircle(
                color = BrandWhite,
                radius = size.minDimension * 0.15f,
                center = Offset(center.x + size.width * 0.15f, center.y - size.height * 0.15f)
            )
        }
    }
}
@Composable
private fun VeggieOpenMouth(modifier: Modifier = Modifier) {
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
private fun VeggieIllustrationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        VeggieIllustration(modifier = Modifier.size(400.dp))
    }
}
