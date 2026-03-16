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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun WholeGrainBakeryIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.whole_grain_bakery_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Wheat Stalks in background
        WheatStalksBackground(modifier = Modifier.fillMaxSize())

        // Baguette in background
        BaguetteBackground(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.TopStart)
                .offset(x = 50.dp, y = 20.dp)
        )

        // Flour Bag (Center Back)
        FlourBag(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.TopCenter)
                .offset(x = 60.dp, y = 40.dp)
        )

        // Bread Loaf (Left Front)
        BreadLoafCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-20).dp)
        )

        // Grain Bowl (Right)
        GrainBowl(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .align(Alignment.BottomEnd)
                .offset(x = (-5).dp, y = (-35).dp)
        )

        // Small Grain Bag (Bottom Left)
        SmallGrainBag(
            modifier = Modifier
                .fillMaxSize(0.28f)
                .align(Alignment.BottomStart)
                .offset(x = 15.dp, y = (-5).dp)
        )

        // Croissant (Front Center)
        CroissantCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.BottomCenter)
                .offset(x = 40.dp, y = 15.dp)
        )

        // Scatters
        GrainScatters(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun FlourBag(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bagPath = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.35f)
                lineTo(size.width * 0.8f, size.height * 0.35f)
                quadraticTo(size.width * 0.9f, size.height * 0.4f, size.width * 0.85f, size.height * 0.9f)
                quadraticTo(size.width * 0.5f, size.height * 0.98f, size.width * 0.15f, size.height * 0.9f)
                quadraticTo(size.width * 0.1f, size.height * 0.4f, size.width * 0.2f, size.height * 0.35f)
                close()
            }
            drawPath(
                path = bagPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F5DC), Color(0xFFE6D2B5))
                )
            )
            drawPath(bagPath, Color(0xFF5D4037), style = Stroke(width = 3f))

            // Flour heap on top
            val flourPath = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.35f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.05f,
                    size.width * 0.7f, size.height * 0.05f,
                    size.width * 0.75f, size.height * 0.35f
                )
            }
            drawPath(flourPath, Color.White)
            drawPath(flourPath, Color(0xFF5D4037), style = Stroke(width = 3f))

            // Blue label
            val labelPath = Path().apply {
                moveTo(size.width * 0.22f, size.height * 0.58f)
                lineTo(size.width * 0.78f, size.height * 0.58f)
                lineTo(size.width * 0.75f, size.height * 0.82f)
                lineTo(size.width * 0.25f, size.height * 0.82f)
                close()
            }
            drawPath(labelPath, Color(0xFF1976D2))
            drawPath(labelPath, Color(0xFF0D47A1), style = Stroke(width = 2f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.42f))
            CharacterFace(
                eyeSize = 10.dp,
                mouthSize = 6.dp,
                eyeSpacing = 4.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "FLOUR",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BreadLoafCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val loafPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.45f)
                cubicTo(
                    size.width * 0.1f, size.height * 0.1f,
                    size.width * 0.9f, size.height * 0.1f,
                    size.width * 0.9f, size.height * 0.45f
                )
                lineTo(size.width * 0.95f, size.height * 0.85f)
                quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.05f, size.height * 0.85f)
                close()
            }
            drawPath(
                path = loafPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8B4513), Color(0xFF5D4037))
                )
            )
            drawPath(loafPath, Color(0xFF3E2723), style = Stroke(width = 4f))

            // Face area (lighter)
            val faceAreaPath = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.48f)
                cubicTo(
                    size.width * 0.25f, size.height * 0.25f,
                    size.width * 0.75f, size.height * 0.25f,
                    size.width * 0.75f, size.height * 0.48f
                )
                lineTo(size.width * 0.85f, size.height * 0.82f)
                quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.15f, size.height * 0.82f)
                close()
            }
            drawPath(faceAreaPath, Color(0xFFFFF3E0))
            drawPath(faceAreaPath, Color(0xFF3E2723), style = Stroke(width = 2f))
        }

        CharacterFace(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .offset(y = 15.dp),
            eyeSize = 14.dp,
            mouthSize = 24.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun CroissantCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val segments = 5
            val centerY = size.height * 0.6f
            val totalWidth = size.width * 0.9f
            val segmentWidth = totalWidth / segments

            for (i in 0 until segments) {
                val scale = if (i == 2) 1.2f else if (i == 1 || i == 3) 1.0f else 0.8f
                val h = size.height * 0.4f * scale
                val w = segmentWidth * 1.1f
                val x = (size.width - totalWidth) / 2 + i * segmentWidth - (w - segmentWidth) / 2
                val y = centerY - h / 2

                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFB74D), Color(0xFFE65100))
                    ),
                    topLeft = Offset(x, y),
                    size = Size(w, h)
                )
                drawOval(
                    color = Color(0xFF3E2723),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    style = Stroke(width = 3f)
                )
            }
        }

        CharacterFace(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .offset(y = 10.dp),
            eyeSize = 12.dp,
            mouthSize = 10.dp
        )
    }
}

@Composable
private fun GrainBowl(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Wooden Bowl
            val bowlPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.5f)
                lineTo(size.width * 0.9f, size.height * 0.5f)
                quadraticTo(size.width * 0.85f, size.height * 0.95f, size.width * 0.5f, size.height * 0.95f)
                quadraticTo(size.width * 0.15f, size.height * 0.95f, size.width * 0.1f, size.height * 0.5f)
                close()
            }
            drawPath(
                path = bowlPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
                )
            )
            drawPath(bowlPath, Color(0xFF3E2723), style = Stroke(width = 4f))

            // Grains inside
            val grainAreaPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.5f)
                quadraticTo(size.width * 0.5f, size.height * 0.35f, size.width * 0.85f, size.height * 0.5f)
            }
            drawPath(grainAreaPath, Color(0xFFFFD54F))
            
            // Grain details
            val grainColor = Color(0xFFFBC02D)
            for (i in 0..15) {
                val rx = (0.2f + Math.random().toFloat() * 0.6f) * size.width
                val ry = (0.45f + Math.random().toFloat() * 0.15f) * size.height
                drawCircle(grainColor, radius = 4f, center = Offset(rx, ry))
            }

            // Spoon
            val spoonPath = Path().apply {
                moveTo(size.width * 0.85f, size.height * 0.55f)
                lineTo(size.width * 1.1f, size.height * 0.2f)
                lineTo(size.width * 1.2f, size.height * 0.3f)
                lineTo(size.width * 0.95f, size.height * 0.65f)
                close()
            }
            rotate(degrees = -15f, pivot = Offset(size.width * 0.9f, size.height * 0.5f)) {
                drawPath(spoonPath, Color(0xFF8D6E63))
                drawPath(spoonPath, Color(0xFF3E2723), style = Stroke(width = 3f))
            }
        }
    }
}

@Composable
private fun SmallGrainBag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val bagPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.7f)
            lineTo(size.width * 0.8f, size.height * 0.7f)
            quadraticTo(size.width * 0.95f, size.height * 0.95f, size.width * 0.5f, size.height * 0.98f)
            quadraticTo(size.width * 0.05f, size.height * 0.95f, size.width * 0.2f, size.height * 0.7f)
            close()
        }
        drawPath(bagPath, Color(0xFFD7CCC8))
        drawPath(bagPath, Color(0xFF5D4037), style = Stroke(width = 3f))

        // Grains on top
        drawOval(
            color = Color(0xFFFFD54F),
            topLeft = Offset(size.width * 0.25f, size.height * 0.65f),
            size = Size(size.width * 0.5f, size.height * 0.15f)
        )
    }
}

@Composable
private fun BaguetteBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        rotate(degrees = -10f) {
            val path = Path().apply {
                moveTo(size.width * 0.3f, size.height)
                lineTo(size.width * 0.3f, size.height * 0.2f)
                quadraticTo(size.width * 0.3f, 0f, size.width * 0.55f, 0f)
                quadraticTo(size.width * 0.8f, 0f, size.width * 0.8f, size.height * 0.2f)
                lineTo(size.width * 0.8f, size.height)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE67E22), Color(0xFFD35400))
                )
            )
            drawPath(path, Color(0xFF5D4037), style = Stroke(width = 4f))

            // Slits
            for (i in 1..4) {
                val slitY = size.height * 0.2f * i
                drawArc(
                    color = Color(0xFF8D6E63),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.4f, slitY),
                    size = Size(size.width * 0.3f, size.height * 0.08f),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

@Composable
private fun WheatStalksBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val wheatColor = Color(0xFFFBC02D)
        
        // Left
        drawWheat(Offset(size.width * 0.15f, size.height * 0.6f), wheatColor, -15f)
        drawWheat(Offset(size.width * 0.25f, size.height * 0.5f), wheatColor, -5f)
        
        // Center
        drawWheat(Offset(size.width * 0.45f, size.height * 0.4f), wheatColor, 0f)
        drawWheat(Offset(size.width * 0.55f, size.height * 0.35f), wheatColor, 10f)

        // Right
        drawWheat(Offset(size.width * 0.85f, size.height * 0.8f), wheatColor, 20f)
    }
}

private fun DrawScope.drawWheat(base: Offset, color: Color, angle: Float) {
    rotate(degrees = angle, pivot = base) {
        val height = size.height * 0.5f
        drawLine(color, base, base.copy(y = base.y - height), strokeWidth = 3f)
        
        for (i in 0..6) {
            val y = base.y - height * (i / 6f)
            drawOval(
                color = color,
                topLeft = Offset(base.x - 12f, y),
                size = Size(24f, 12f)
            )
        }
    }
}

@Composable
private fun GrainScatters(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val grainColor = Color(0xFFFFD54F)
        val scatterPositions = listOf(
            Offset(0.2f, 0.95f), Offset(0.25f, 0.92f), Offset(0.3f, 0.96f),
            Offset(0.5f, 0.98f), Offset(0.55f, 0.94f), Offset(0.6f, 0.97f),
            Offset(0.85f, 0.9f), Offset(0.9f, 0.93f)
        )
        
        scatterPositions.forEach { pos ->
            drawCircle(
                color = grainColor,
                radius = 5f,
                center = Offset(pos.x * size.width, pos.y * size.height)
            )
        }
    }
}

@Composable
private fun CharacterFace(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 12.dp,
    mouthSize: Dp = 10.dp,
    eyeSpacing: Dp = 8.dp,
    isOpenMouth: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(eyeSpacing)) {
            CharacterEye(eyeSize = eyeSize)
            CharacterEye(eyeSize = eyeSize)
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (isOpenMouth) {
            OpenMouth(modifier = Modifier.size(mouthSize))
        } else {
            Mouth(modifier = Modifier.size(mouthSize))
        }
    }
}

@Composable
private fun CharacterEye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(Color.White, CircleShape)
            .border(1.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension * 0.35f,
                center = center.copy(y = center.y + size.height * 0.05f)
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.12f,
                center = center.copy(x = center.x + size.width * 0.15f, y = center.y - size.height * 0.15f)
            )
        }
    }
}

@Composable
private fun Mouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height, size.width, size.height * 0.2f)
        }
        drawPath(path, Color.Black, style = Stroke(width = 3f))
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
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, Color.Black, style = Stroke(width = 2f))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.7f)
            quadraticTo(size.width / 2, size.height * 0.95f, size.width * 0.8f, size.height * 0.7f)
        }
        drawPath(tonguePath, Color(0xFFFF80AB))
    }
}

@Preview(showBackground = true)
@Composable
private fun WholeGrainBakeryIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            WholeGrainBakeryIllustration(modifier = Modifier.size(400.dp))
        }
    }
}
