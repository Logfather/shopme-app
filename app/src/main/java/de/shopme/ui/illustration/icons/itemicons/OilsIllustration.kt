package de.shopme.ui.illustration.icons.itemicons
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
@Composable
fun OilsIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.oils_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Olive Oil Bottle (Back Left)
        OliveOilBottle(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-20).dp)
        )
        // Sunflower Oil Bottle (Back Right)
        SunflowerOilBottle(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-30).dp)
        )
        // Sunflower (Far Right)
        Sunflower(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = (-20).dp)
        )
        // Seeds Bowl (Middle Right)
        SunflowerSeedsBowl(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = (-10).dp)
        )
        // Small Oil Jar (Front Center)
        SmallOilJar(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomCenter)
                .offset(x = (-10).dp, y = (-5).dp)
        )
        // Olives and Leaves (Front Left)
        OlivesAndLeaves(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = 5.dp)
        )
        // Oil Droplets
        OilDroplets(modifier = Modifier.fillMaxSize())
    }
}
@Composable
private fun OliveOilBottle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Bottle Body
            val bottlePath = Path().apply {
                moveTo(w * 0.25f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.2f)
                lineTo(w * 0.85f, h * 0.9f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.15f, h * 0.9f)
                close()
            }
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF558B2F), Color(0xFF33691E))
                )
            )
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 4f))
            // Neck and Cap
            val capPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.4f, h * 0.05f, w * 0.6f, h * 0.2f),
                        cornerRadius = CornerRadius(10f)
                    )
                )
            }
            drawPath(capPath, Color(0xFF212121))
            drawPath(capPath, BrandBlack, style = Stroke(width = 3f))
            // Label
            drawRoundRect(
                color = Color(0xFFFFF9C4),
                topLeft = Offset(w * 0.25f, h * 0.45f),
                size = Size(w * 0.5f, h * 0.25f),
                cornerRadius = CornerRadius(8f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.25f, h * 0.45f),
                size = Size(w * 0.5f, h * 0.25f),
                cornerRadius = CornerRadius(8f),
                style = Stroke(width = 2f)
            )

            // Label Illustration (simple olive)
            drawCircle(Color(0xFF8BC34A), radius = 10f, center = Offset(w * 0.45f, h * 0.55f))
            drawCircle(Color(0xFF8BC34A), radius = 10f, center = Offset(w * 0.55f, h * 0.58f))
        }

        CharacterFace(
            modifier = Modifier.offset(y = (-30).dp),
            eyeSize = 24.dp,
            mouthSize = 30.dp
        )
    }
}
@Composable
private fun SunflowerOilBottle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Ridged Bottle Body
            val bottlePath = Path().apply {
                moveTo(w * 0.3f, h * 0.25f)
                lineTo(w * 0.7f, h * 0.25f)
                lineTo(w * 0.8f, h * 0.45f)
                lineTo(w * 0.85f, h * 0.45f)
                lineTo(w * 0.85f, h * 0.9f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.15f, h * 0.9f)
                lineTo(w * 0.15f, h * 0.45f)
                lineTo(w * 0.2f, h * 0.45f)
                close()
            }
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFBC02D))
                )
            )
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 4f))
            // Neck and Cap
            val capPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.4f, h * 0.1f, w * 0.6f, h * 0.25f),
                        cornerRadius = CornerRadius(10f)
                    )
                )
            }
            drawPath(capPath, Color(0xFFFFEB3B))
            drawPath(capPath, BrandBlack, style = Stroke(width = 3f))
            // Ridges
            for (i in 0..3) {
                drawLine(
                    color = BrandBlack.copy(alpha = 0.2f),
                    start = Offset(w * 0.15f, h * (0.5f + i * 0.1f)),
                    end = Offset(w * 0.85f, h * (0.5f + i * 0.1f)),
                    strokeWidth = 2f
                )
            }
        }

        CharacterFace(
            modifier = Modifier.offset(y = (-20).dp),
            eyeSize = 28.dp,
            mouthSize = 36.dp
        )
    }
}
@Composable
private fun SmallOilJar(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Jar Body (Round)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE58), Color(0xFFFBC02D)),
                    center = center.copy(y = h * 0.6f),
                    radius = w * 0.4f
                ),
                radius = w * 0.4f,
                center = center.copy(y = h * 0.6f)
            )
            drawCircle(BrandBlack, radius = w * 0.4f, center = center.copy(y = h * 0.6f), style = Stroke(width = 3f))
            // Neck
            val neckPath = Path().apply {
                addRect(Rect(w * 0.35f, h * 0.25f, w * 0.65f, h * 0.45f))
            }
            drawPath(neckPath, BrandWhite.copy(alpha = 0.5f))
            drawPath(neckPath, BrandBlack, style = Stroke(width = 3f))
            // Cork
            val corkPath = Path().apply {
                moveTo(w * 0.3f, h * 0.15f)
                lineTo(w * 0.7f, h * 0.15f)
                lineTo(w * 0.65f, h * 0.3f)
                lineTo(w * 0.35f, h * 0.3f)
                close()
            }
            drawPath(corkPath, Color(0xFF8D6E63))
            drawPath(corkPath, BrandBlack, style = Stroke(width = 3f))

            // Handle
            val handlePath = Path().apply {
                arcTo(Rect(w * 0.6f, h * 0.35f, w * 0.9f, h * 0.65f), 270f, 180f, false)
            }
            drawPath(handlePath, BrandBlack, style = Stroke(width = 3f))
        }
    }
}
@Composable
private fun SunflowerSeedsBowl(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Bowl
            val bowlPath = Path().apply {
                moveTo(w * 0.1f, h * 0.6f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.9f, h * 0.6f)
                close()
            }
            drawPath(path = bowlPath, color = Color(0xFF795548))
            drawPath(bowlPath, BrandBlack, style = Stroke(width = 3f))
            // Seeds
            for (i in 0..12) {
                drawOval(
                    color = Color(0xFF424242),
                    topLeft = Offset(w * (0.3f + (i % 4) * 0.12f), h * (0.55f + (i / 4) * 0.05f)),
                    size = Size(15f, 10f)
                )
            }
        }
    }
}
@Composable
private fun Sunflower(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.5f)
            // Petals
            for (i in 0 until 12) {
                rotate(i * 30f, pivot = center) {
                    drawOval(
                        color = Color(0xFFFFEB3B),
                        topLeft = Offset(center.x - 10f, center.y - h * 0.45f),
                        size = Size(20f, h * 0.4f)
                    )
                    drawOval(
                        color = BrandBlack,
                        topLeft = Offset(center.x - 10f, center.y - h * 0.45f),
                        size = Size(20f, h * 0.4f),
                        style = Stroke(width = 1f)
                    )
                }
            }
            // Brown Center
            drawCircle(Color(0xFF5D4037), radius = w * 0.25f, center = center)
            drawCircle(BrandBlack, radius = w * 0.25f, center = center, style = Stroke(width = 2f))

            // Seed dots
            for (i in 0..20) {
                drawCircle(Color(0xFF3E2723), radius = 3f, center = Offset(center.x + (Math.random().toFloat() - 0.5f) * w * 0.3f, center.y + (Math.random().toFloat() - 0.5f) * h * 0.3f))
            }
        }
    }
}
@Composable
private fun OlivesAndLeaves(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Leaves
            drawLeaf(Offset(w * 0.3f, h * 0.4f), 40f, 45f)
            drawLeaf(Offset(w * 0.2f, h * 0.6f), 35f, -30f)
            drawLeaf(Offset(w * 0.5f, h * 0.8f), 45f, 120f)
            // Olives
            drawOlive(Offset(w * 0.4f, h * 0.7f), 22f)
            drawOlive(Offset(w * 0.7f, h * 0.75f), 24f)
            drawOlive(Offset(w * 0.6f, h * 0.6f), 20f)
        }
    }
}
private fun DrawScope.drawLeaf(center: Offset, size: Float, rotationDegrees: Float) {
    rotate(rotationDegrees, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            quadraticTo(center.x + size, center.y, center.x, center.y + size)
            quadraticTo(center.x - size, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, Color(0xFF689F38))
        drawPath(path, BrandBlack, style = Stroke(width = 2f))
        drawLine(BrandBlack, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 1f)
    }
}
private fun DrawScope.drawOlive(center: Offset, radius: Float) {
    drawOval(Color(0xFF8BC34A), topLeft = Offset(center.x - radius, center.y - radius * 0.7f), size = Size(radius * 2, radius * 1.4f))
    drawOval(BrandBlack, topLeft = Offset(center.x - radius, center.y - radius * 0.7f), size = Size(radius * 2, radius * 1.4f), style = Stroke(width = 2f))
    // Highlight
    drawCircle(BrandWhite.copy(alpha = 0.3f), radius = radius * 0.3f, center = Offset(center.x - radius * 0.4f, center.y - radius * 0.2f))
}
@Composable
private fun OilDroplets(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawDroplet(Offset(w * 0.25f, h * 0.85f), 15f, Color(0xFF8BC34A))
        drawDroplet(Offset(w * 0.6f, h * 0.9f), 18f, Color(0xFFFFEB3B))
    }
}
private fun DrawScope.drawDroplet(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x + size * 0.8f, center.y + size * 0.2f, center.x, center.y + size)
        quadraticTo(center.x - size * 0.8f, center.y + size * 0.2f, center.x, center.y - size)
        close()
    }
    drawPath(path, color)
    drawPath(path, BrandBlack, style = Stroke(width = 2f))
    // Highlight
    drawCircle(BrandWhite.copy(alpha = 0.4f), radius = size * 0.2f, center = Offset(center.x - size * 0.2f, center.y + size * 0.3f))
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
            .border(2.dp, BrandBlack, CircleShape),
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
        drawPath(path, BrandBlack, style = Stroke(width = 3f))
    }
}
@Preview(showBackground = true)
@Composable
private fun OilsIllustrationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        OilsIllustration(modifier = Modifier.size(400.dp))
    }
}
