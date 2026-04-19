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
fun VeganIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.vegan_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Background Leaves
        BackgroundLeaves(modifier = Modifier.fillMaxSize())
        // Plant Milk Bottle (Back)
        PlantMilkBottle(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.TopCenter)
                .offset(x = 20.dp, y = 30.dp)
        )
        // Tofu Character (Left)
        TofuCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterStart)
                .offset(x = 30.dp, y = (-10).dp)
        )
        // Tomato Character (Bottom Left)
        TomatoCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.BottomStart)
                .offset(x = 15.dp, y = (-60).dp)
        )
        // Avocado Character (Right)
        AvocadoCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-40).dp)
        )
        // Foreground Decorations (Nuts, seeds, cherry tomatoes)
        ForegroundVegetables(modifier = Modifier.fillMaxSize())
        // Vegan Ribbon (Front)
        VeganRibbon(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-40).dp)
        )
    }
}
@Composable
private fun BackgroundLeaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Dark green kale/broccoli shapes in the back
        drawLeafCloud(Offset(w * 0.3f, h * 0.3f), 100f, Color(0xFF2E7D32))
        drawLeafCloud(Offset(w * 0.7f, h * 0.35f), 120f, Color(0xFF1B5E20))
        drawLeafCloud(Offset(w * 0.5f, h * 0.25f), 110f, Color(0xFF388E3C))
        // Lighter green leaves
        drawSimpleLeaf(Offset(w * 0.15f, h * 0.45f), 60f, 45f, Color(0xFF4CAF50))
        drawSimpleLeaf(Offset(w * 0.85f, h * 0.55f), 70f, -30f, Color(0xFF81C784))
    }
}
private fun DrawScope.drawLeafCloud(center: Offset, radius: Float, color: Color) {
    for (i in 0 until 6) {
        val angle = i * 60f
        val offset = Offset(
            x = center.x + radius * 0.5f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
            y = center.y + radius * 0.5f * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        drawCircle(color, radius = radius * 0.6f, center = offset)
        drawCircle(BrandBlack, radius = radius * 0.6f, center = offset, style = Stroke(width = 3f))
    }
}
private fun DrawScope.drawSimpleLeaf(center: Offset, size: Float, rotation: Float, color: Color) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            quadraticTo(center.x + size * 0.7f, center.y, center.x, center.y + size)
            quadraticTo(center.x - size * 0.7f, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, color)
        drawPath(path, BrandBlack, style = Stroke(width = 3f))
        drawLine(BrandBlack, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 2f)
    }
}
@Composable
private fun PlantMilkBottle(modifier: Modifier = Modifier) {
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
                    colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4))
                )
            )
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 4f))
            // Cap
            val capPath = Path().apply {
                moveTo(w * 0.33f, h * 0.05f)
                lineTo(w * 0.67f, h * 0.05f)
                lineTo(w * 0.65f, h * 0.15f)
                lineTo(w * 0.35f, h * 0.15f)
                close()
            }
            drawPath(capPath, Color(0xFF0288D1))
            drawPath(capPath, BrandBlack, style = Stroke(width = 4f))
            // Label
            val labelPath = Path().apply {
                moveTo(w * 0.18f, h * 0.55f)
                lineTo(w * 0.82f, h * 0.52f)
                lineTo(w * 0.84f, h * 0.78f)
                lineTo(w * 0.16f, h * 0.81f)
                close()
            }
            drawPath(labelPath, Color(0xFF4FC3F7))
            drawPath(labelPath, BrandBlack, style = Stroke(width = 3f))
        }
        Text(
            text = stringResource(R.string.plant_milk_label),
            color = BrandWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier
                .offset(y = 40.dp)
                .rotate(-3f)
        )
    }
}
@Composable
private fun TofuCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Tofu Cube (perspective)
            val frontPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = w * 0.1f,
                        top = h * 0.3f,
                        right = w * 0.8f,
                        bottom = h * 0.9f,
                        cornerRadius = CornerRadius(20f, 20f)
                    )
                )
            }
            drawPath(frontPath, Color(0xFFFFF9C4))
            drawPath(frontPath, BrandBlack, style = Stroke(width = 4f))
            // Side/Top for perspective
            val topPath = Path().apply {
                moveTo(w * 0.1f, h * 0.3f)
                lineTo(w * 0.3f, h * 0.2f)
                lineTo(w * 0.9f, h * 0.25f)
                lineTo(w * 0.8f, h * 0.35f)
                close()
            }
            drawPath(topPath, Color(0xFFFFF176))
            drawPath(topPath, BrandBlack, style = Stroke(width = 4f))
            val sidePath = Path().apply {
                moveTo(w * 0.8f, h * 0.35f)
                lineTo(w * 0.9f, h * 0.25f)
                lineTo(w * 0.95f, h * 0.8f)
                lineTo(w * 0.8f, h * 0.9f)
                close()
            }
            drawPath(sidePath, Color(0xFFFDD835))
            drawPath(sidePath, BrandBlack, style = Stroke(width = 4f))
        }
        Column(
            modifier = Modifier.padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterEye(eyeSize = 24.dp)
                CharacterEye(eyeSize = 24.dp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            OpenMouth(modifier = Modifier.size(32.dp, 20.dp))
        }
    }
}
@Composable
private fun AvocadoCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Outer Dark Green Skin
            val skinPath = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                quadraticTo(w * 0.85f, h * 0.2f, w * 0.9f, h * 0.6f)
                quadraticTo(w * 0.95f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.05f, h * 0.95f, w * 0.1f, h * 0.6f)
                quadraticTo(w * 0.15f, h * 0.2f, w * 0.5f, h * 0.1f)
                close()
            }
            drawPath(skinPath, Color(0xFF1B5E20))
            drawPath(skinPath, BrandBlack, style = Stroke(width = 4f))
            // Light Green Flesh
            val fleshPath = Path().apply {
                moveTo(w * 0.5f, h * 0.18f)
                quadraticTo(w * 0.78f, h * 0.25f, w * 0.82f, h * 0.6f)
                quadraticTo(w * 0.85f, h * 0.88f, w * 0.5f, h * 0.88f)
                quadraticTo(w * 0.15f, h * 0.88f, w * 0.18f, h * 0.6f)
                quadraticTo(w * 0.22f, h * 0.25f, w * 0.5f, h * 0.18f)
                close()
            }
            drawPath(
                path = fleshPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFC5E1A5), Color(0xFFDCEDC8))
                )
            )
            drawPath(fleshPath, BrandBlack, style = Stroke(width = 3f))
            // The Seed (Pit) is also the Mouth in this design
            drawCircle(
                color = Color(0xFF5D4037),
                radius = w * 0.15f,
                center = Offset(w * 0.52f, h * 0.68f)
            )
            drawCircle(
                color = BrandBlack,
                radius = w * 0.15f,
                center = Offset(w * 0.52f, h * 0.68f),
                style = Stroke(width = 4f)
            )

            // Highlight on pit
            drawCircle(
                color = BrandWhite.copy(alpha = 0.3f),
                radius = w * 0.04f,
                center = Offset(w * 0.58f, h * 0.62f)
            )
        }
        Column(
            modifier = Modifier.offset(y = (-30).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CharacterEye(eyeSize = 22.dp)
                CharacterEye(eyeSize = 22.dp)
            }
        }
    }
}
@Composable
private fun TomatoCharacter(modifier: Modifier = Modifier) {
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
                CharacterEye(eyeSize = 18.dp)
                CharacterEye(eyeSize = 18.dp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            OpenMouth(modifier = Modifier.size(24.dp, 14.dp))
        }
    }
}
@Composable
private fun ForegroundVegetables(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Small tomatoes
        drawCherryTomato(Offset(w * 0.38f, h * 0.72f), 15f)
        drawCherryTomato(Offset(w * 0.45f, h * 0.75f), 18f)
        // Nuts/Seeds
        drawSeed(Offset(w * 0.25f, h * 0.78f), 12f, 20f)
        drawSeed(Offset(w * 0.32f, h * 0.82f), 14f, -10f)
        drawSeed(Offset(w * 0.4f, h * 0.85f), 15f, 45f)
    }
}
private fun DrawScope.drawCherryTomato(center: Offset, radius: Float) {
    drawCircle(Color(0xFFD32F2F), radius = radius, center = center)
    drawCircle(BrandBlack, radius = radius, center = center, style = Stroke(width = 2f))
}
private fun DrawScope.drawSeed(center: Offset, size: Float, rotation: Float) {
    rotate(rotation, center) {
        drawOval(
            color = Color(0xFFD2B48C),
            topLeft = Offset(center.x - size, center.y - size * 0.6f),
            size = Size(size * 2, size * 1.2f)
        )
        drawOval(
            color = BrandBlack,
            topLeft = Offset(center.x - size, center.y - size * 0.6f),
            size = Size(size * 2, size * 1.2f),
            style = Stroke(width = 2f)
        )
    }
}
@Composable
private fun VeganRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Ribbon main part
            val ribbonPath = Path().apply {
                moveTo(w * 0.1f, h * 0.3f)
                quadraticTo(w * 0.5f, h * 0.25f, w * 0.9f, h * 0.3f)
                lineTo(w * 0.95f, h * 0.8f)
                quadraticTo(w * 0.5f, h * 0.85f, w * 0.05f, h * 0.8f)
                close()
            }
            drawPath(path = ribbonPath, color = Color(0xFF8BC34A))
            drawPath(path = ribbonPath, color = BrandBlack, style = Stroke(width = 5f))
            // Ribbon folds (back)
            val leftFold = Path().apply {
                moveTo(w * 0.1f, h * 0.35f)
                lineTo(0f, h * 0.2f)
                lineTo(w * 0.05f, h * 0.5f)
                lineTo(0f, h * 0.8f)
                lineTo(w * 0.1f, h * 0.75f)
                close()
            }
            drawPath(path = leftFold, color = Color(0xFF689F38))
            drawPath(path = leftFold, color = BrandBlack, style = Stroke(width = 4f))
            val rightFold = Path().apply {
                moveTo(w * 0.9f, h * 0.35f)
                lineTo(w, h * 0.2f)
                lineTo(w * 0.95f, h * 0.5f)
                lineTo(w, h * 0.8f)
                lineTo(w * 0.9f, h * 0.75f)
                close()
            }
            drawPath(path = rightFold, color = Color(0xFF689F38))
            drawPath(path = rightFold, color = BrandBlack, style = Stroke(width = 4f))
        }
        Text(
            text = stringResource(R.string.vegan_label),
            color = BrandWhite,
            fontSize = 56.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.offset(y = 10.dp)
        )
    }
}
@Composable
private fun CharacterEye(eyeSize: Dp) {
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
private fun VeganIllustrationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        VeganIllustration(modifier = Modifier.size(400.dp))
    }
}
