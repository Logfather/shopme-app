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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun SpicesIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.spices_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = Color.LightGray.copy(alpha = 0.3f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
                size = Size(size.width * 0.8f, size.height * 0.2f)
            )
        }

        // Back Row: Oil Bottle (Right), Paprika (Center), Salt (Left)
        OilBottle(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.CenterEnd)
                .offset(x = (-20).dp, y = (-20).dp)
        )

        SaltShaker(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = (-10).dp)
        )

        PaprikaJar(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.Center)
                .offset(y = (-30).dp)
        )

        // Middle Row: Herbs (Right), Spice Bowl (Right-ish), Chili Peppers (Left)
        Herbs(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-40).dp)
        )

        SpiceBowl(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-20).dp)
        )

        ChiliPepper(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-10).dp),
            isRed = true
        )

        ChiliPepper(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomStart)
                .offset(x = 60.dp, y = 5.dp),
            isRed = false
        )

        // Front Row: Garlic, Star Anise
        Garlic(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomCenter)
                .offset(x = (-20).dp, y = 10.dp)
        )

        StarAnise(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-60).dp, y = 15.dp)
        )

        // Scattered Peppercorns
        Peppercorns(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SaltShaker(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glass Body
            val bodyPath = Path().apply {
                moveTo(w * 0.25f, h * 0.35f)
                lineTo(w * 0.75f, h * 0.35f)
                lineTo(w * 0.85f, h * 0.9f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.15f, h * 0.9f)
                close()
            }
            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.8f), Color(0xFFEEEEEE))
                )
            )
            drawPath(bodyPath, Color.Black, style = Stroke(width = 4f))

            // Salt Grain effect
            for (i in 0..20) {
                drawCircle(Color.White, radius = 2f, center = Offset(w * (0.3f + Math.random().toFloat() * 0.4f), h * (0.5f + Math.random().toFloat() * 0.4f)))
            }

            // Cap
            val capPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.2f, h * 0.25f, w * 0.8f, h * 0.4f),
                        cornerRadius = CornerRadius(10f)
                    )
                )
            }
            drawPath(capPath, Color(0xFFBDBDBD))
            drawPath(capPath, Color.Black, style = Stroke(width = 4f))

            // Holes
            for (i in 0..4) {
                drawCircle(Color.DarkGray, radius = 3f, center = Offset(w * (0.3f + i * 0.1f), h * 0.3f))
            }
        }
        CharacterFace(
            modifier = Modifier.offset(y = 15.dp),
            eyeSize = 18.dp,
            mouthSize = 24.dp
        )
    }
}

@Composable
private fun PaprikaJar(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Jar Body
            val jarPath = Path().apply {
                moveTo(w * 0.2f, h * 0.3f)
                lineTo(w * 0.8f, h * 0.3f)
                lineTo(w * 0.85f, h * 0.85f)
                quadraticTo(w * 0.85f, h * 0.9f, w * 0.5f, h * 0.9f)
                quadraticTo(w * 0.15f, h * 0.9f, w * 0.15f, h * 0.85f)
                close()
            }
            drawPath(
                path = jarPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFB74D), Color(0xFFE65100))
                )
            )
            drawPath(jarPath, Color.Black, style = Stroke(width = 4f))

            // Powder Texture
            for (i in 0..100) {
                drawCircle(
                    color = if (i % 2 == 0) Color(0xFFD84315) else Color(0xFFFF9800),
                    radius = 2f,
                    center = Offset(w * (0.25f + Math.random().toFloat() * 0.5f), h * (0.4f + Math.random().toFloat() * 0.45f))
                )
            }

            // Red Lid
            val lidPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.18f, h * 0.15f, w * 0.82f, h * 0.32f),
                        cornerRadius = CornerRadius(12f)
                    )
                )
            }
            drawPath(lidPath, Color(0xFFD32F2F))
            drawPath(lidPath, Color.Black, style = Stroke(width = 4f))
            
            // Lid Ridges
            for (i in 0..10) {
                drawLine(Color.Black, Offset(w * (0.22f + i * 0.055f), h * 0.15f), Offset(w * (0.22f + i * 0.055f), h * 0.32f), strokeWidth = 2f)
            }

            // Red Label
            drawRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(w * 0.2f, h * 0.4f),
                size = Size(w * 0.6f, h * 0.15f)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(w * 0.2f, h * 0.4f),
                size = Size(w * 0.6f, h * 0.15f),
                style = Stroke(width = 3f)
            )
        }

        Text(
            text = stringResource(R.string.paprika_label),
            color = Color(0xFFFFF176),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(y = (-15).dp)
        )

        CharacterFace(
            modifier = Modifier.offset(y = 35.dp),
            eyeSize = 28.dp,
            mouthSize = 40.dp
        )
    }
}

@Composable
private fun OilBottle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bottle Body
            val bottlePath = Path().apply {
                moveTo(w * 0.35f, h * 0.3f)
                lineTo(w * 0.65f, h * 0.3f)
                quadraticTo(w * 0.85f, h * 0.4f, w * 0.85f, h * 0.7f)
                quadraticTo(w * 0.85f, h * 0.9f, w * 0.5f, h * 0.9f)
                quadraticTo(w * 0.15f, h * 0.9f, w * 0.15f, h * 0.7f)
                quadraticTo(w * 0.15f, h * 0.4f, w * 0.35f, h * 0.3f)
                close()
            }
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFC5E1A5), Color(0xFF8BC34A))
                )
            )
            drawPath(bottlePath, Color.Black, style = Stroke(width = 4f))

            // Neck
            val neckPath = Path().apply {
                addRect(Rect(w * 0.4f, h * 0.15f, w * 0.6f, h * 0.3f))
            }
            drawPath(neckPath, Color(0xFF8BC34A))
            drawPath(neckPath, Color.Black, style = Stroke(width = 4f))

            // Cap
            val capPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.35f, h * 0.05f, w * 0.65f, h * 0.15f),
                        cornerRadius = CornerRadius(5f)
                    )
                )
            }
            drawPath(capPath, Color(0xFF424242))
            drawPath(capPath, Color.Black, style = Stroke(width = 3f))

            // Handle
            val handlePath = Path().apply {
                arcTo(Rect(w * 0.7f, h * 0.25f, w * 0.95f, h * 0.55f), 270f, 180f, false)
            }
            drawPath(handlePath, Color.Black, style = Stroke(width = 4f))
            
            // Tag
            drawRect(Color(0xFFFFF9C4), topLeft = Offset(w * 0.65f, h * 0.35f), size = Size(w * 0.25f, h * 0.2f))
            drawRect(Color.Black, topLeft = Offset(w * 0.65f, h * 0.35f), size = Size(w * 0.25f, h * 0.2f), style = Stroke(width = 2f))
            drawCircle(Color(0xFF33691E), radius = 8f, center = Offset(w * 0.77f, h * 0.45f))
        }
        CharacterFace(
            modifier = Modifier.offset(y = 15.dp),
            eyeSize = 22.dp,
            mouthSize = 30.dp
        )
    }
}

@Composable
private fun ChiliPepper(modifier: Modifier = Modifier, isRed: Boolean) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val pepperPath = Path().apply {
                moveTo(w * 0.4f, h * 0.2f)
                quadraticTo(w * 0.9f, h * 0.3f, w * 0.7f, h * 0.85f)
                quadraticTo(w * 0.65f, h * 0.95f, w * 0.5f, h * 0.9f)
                quadraticTo(w * 0.1f, h * 0.6f, w * 0.4f, h * 0.2f)
                close()
            }
            drawPath(pepperPath, if (isRed) Color(0xFFD32F2F) else Color(0xFF43A047))
            drawPath(pepperPath, Color.Black, style = Stroke(width = 4f))

            // Stem
            val stemPath = Path().apply {
                moveTo(w * 0.4f, h * 0.2f)
                quadraticTo(w * 0.35f, h * 0.05f, w * 0.2f, h * 0.1f)
            }
            drawPath(stemPath, Color(0xFF2E7D32), style = Stroke(width = 6f))
        }
        CharacterFace(
            modifier = Modifier.offset(x = (-5).dp, y = 5.dp),
            eyeSize = 16.dp,
            mouthSize = 20.dp
        )
    }
}

@Composable
private fun Garlic(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Garlic Bulb
            val garlicPath = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                quadraticTo(w * 0.15f, h * 0.4f, w * 0.15f, h * 0.75f)
                quadraticTo(w * 0.15f, h * 0.95f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.85f, h * 0.95f, w * 0.85f, h * 0.75f)
                quadraticTo(w * 0.85f, h * 0.4f, w * 0.5f, h * 0.1f)
                close()
            }
            drawPath(garlicPath, Color(0xFFF5F5F5))
            drawPath(garlicPath, Color.Black, style = Stroke(width = 3f))

            // Segments
            drawLine(Color.Black.copy(alpha = 0.2f), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.95f), strokeWidth = 2f)
            drawLine(Color.Black.copy(alpha = 0.2f), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.3f, h * 0.85f), strokeWidth = 2f)
            drawLine(Color.Black.copy(alpha = 0.2f), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.7f, h * 0.85f), strokeWidth = 2f)
        }
        CharacterFace(
            modifier = Modifier.offset(y = 20.dp),
            eyeSize = 14.dp,
            mouthSize = 18.dp
        )
    }
}

@Composable
private fun SpiceBowl(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bowl
        val bowlPath = Path().apply {
            moveTo(w * 0.05f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.95f, w * 0.95f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.4f, w * 0.05f, h * 0.5f)
            close()
        }
        drawPath(bowlPath, Color(0xFF8D6E63))
        drawPath(bowlPath, Color.Black, style = Stroke(width = 4f))

        // Spices Heap
        val heapPath = Path().apply {
            moveTo(w * 0.15f, h * 0.48f)
            quadraticTo(w * 0.5f, h * 0.15f, w * 0.85f, h * 0.48f)
            close()
        }
        drawPath(heapPath, Color(0xFFE65100))
        
        // Multi-colored grains
        for (i in 0..40) {
            drawCircle(
                color = when (i % 3) {
                    0 -> Color.Black
                    1 -> Color(0xFFD32F2F)
                    else -> Color(0xFFFFF176)
                },
                radius = 3f,
                center = Offset(w * (0.2f + Math.random().toFloat() * 0.6f), h * (0.35f + Math.random().toFloat() * 0.2f))
            )
        }
    }
}

@Composable
private fun StarAnise(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)

        for (i in 0 until 8) {
            rotate(i * 45f, pivot = center) {
                val petalPath = Path().apply {
                    moveTo(center.x, center.y)
                    quadraticTo(center.x + w * 0.15f, center.y - h * 0.4f, center.x, center.y - h * 0.5f)
                    quadraticTo(center.x - w * 0.15f, center.y - h * 0.4f, center.x, center.y)
                    close()
                }
                drawPath(petalPath, Color(0xFF5D4037))
                drawPath(petalPath, Color.Black, style = Stroke(width = 2f))
                drawCircle(Color(0xFF3E2723), radius = 5f, center = Offset(center.x, center.y - h * 0.35f))
            }
        }
    }
}

@Composable
private fun Herbs(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Leaves bundle
        drawLeaf(Offset(w * 0.5f, h * 0.3f), 40f, 0f)
        drawLeaf(Offset(w * 0.35f, h * 0.45f), 35f, -30f)
        drawLeaf(Offset(w * 0.65f, h * 0.45f), 35f, 30f)
        drawLeaf(Offset(w * 0.25f, h * 0.7f), 30f, -60f)
        drawLeaf(Offset(w * 0.75f, h * 0.7f), 30f, 60f)
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
        drawPath(path, Color(0xFF43A047))
        drawPath(path, Color.Black, style = Stroke(width = 2f))
        drawLine(Color.Black, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 1f)
    }
}

@Composable
private fun Peppercorns(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val random = java.util.Random(42)
        for (i in 0..15) {
            val color = when (random.nextInt(4)) {
                0 -> Color.Black
                1 -> Color(0xFFD32F2F)
                2 -> Color(0xFFFFF176)
                else -> Color(0xFF8D6E63)
            }
            drawCircle(
                color = color,
                radius = 4f,
                center = Offset(w * (0.1f + random.nextFloat() * 0.8f), h * (0.85f + random.nextFloat() * 0.1f))
            )
            drawCircle(
                color = Color.Black,
                radius = 4f,
                center = Offset(w * (0.1f + random.nextFloat() * 0.8f), h * (0.85f + random.nextFloat() * 0.1f)),
                style = Stroke(width = 1f)
            )
        }
    }
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
            .background(Color.White, CircleShape)
            .border(2.dp, Color.Black, CircleShape),
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
        drawPath(path, Color.Black, style = Stroke(width = 3f))
    }
}

@Preview(showBackground = true)
@Composable
private fun SpicesIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SpicesIllustration(modifier = Modifier.size(400.dp))
        }
    }
}
