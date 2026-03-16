package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun VegetablesGroupIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.vegetables_group_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                topLeft = Offset(size.width * 0.15f, size.height * 0.85f),
                size = Size(size.width * 0.7f, size.height * 0.1f)
            )
        }

        BackgroundVegetableLeaves(modifier = Modifier.fillMaxSize())

        CornCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopStart)
                .offset(x = 60.dp, y = 30.dp)
        )

        GarlicCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.TopCenter)
                .offset(x = 20.dp, y = 55.dp)
        )

        EggplantCharacter(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 60.dp)
        )

        CarrotCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = 0.dp)
        )

        CucumberCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterEnd)
                .offset(x = (-15).dp, y = 20.dp)
        )

        TomatoCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomCenter)
                .offset(x = (-10).dp, y = (-15).dp)
        )

        BroccoliCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-20).dp)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSparkle(Offset(size.width * 0.43f, size.height * 0.18f), 15f)
            drawSparkle(Offset(size.width * 0.58f, size.height * 0.22f), 12f)
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, Color(0xFFFFD54F))
    drawPath(path, Color.Black, style = Stroke(width = 2f))
}

@Composable
private fun BackgroundVegetableLeaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawLeafCloud(Offset(size.width * 0.75f, size.height * 0.4f), 80f, Color(0xFF4CAF50))
        drawLeafCloud(Offset(size.width * 0.25f, size.height * 0.7f), 70f, Color(0xFF8BC34A))
        
        drawSimpleLeaf(Offset(size.width * 0.1f, size.height * 0.58f), 40f, 45f, Color(0xFF81C784))
        drawSimpleLeaf(Offset(size.width * 0.85f, size.height * 0.22f), 45f, -30f, Color(0xFF66BB6A))
        drawSimpleLeaf(Offset(size.width * 0.72f, size.height * 0.25f), 30f, 15f, Color(0xFFA5D6A7))
    }
}

private fun DrawScope.drawLeafCloud(center: Offset, radius: Float, color: Color) {
    for (i in 0 until 5) {
        val angle = i * 72f
        val offset = Offset(
            x = center.x + radius * 0.4f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
            y = center.y + radius * 0.4f * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        drawCircle(color, radius = radius * 0.7f, center = offset)
        drawCircle(Color.Black, radius = radius * 0.7f, center = offset, style = Stroke(width = 3f))
    }
}

private fun DrawScope.drawSimpleLeaf(center: Offset, size: Float, rotation: Float, color: Color) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            quadraticTo(center.x + size * 0.8f, center.y, center.x, center.y + size)
            quadraticTo(center.x - size * 0.8f, center.y, center.x, center.y - size)
            close()
        }
        drawPath(path, color)
        drawPath(path, Color.Black, style = Stroke(width = 3f))
        drawLine(Color.Black, Offset(center.x, center.y - size * 0.8f), Offset(center.x, center.y + size * 0.8f), strokeWidth = 2f)
    }
}

@Composable
private fun TomatoCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F)),
                    center = Offset(w * 0.4f, h * 0.4f)
                ),
                radius = w * 0.45f,
                center = center
            )
            drawCircle(Color.Black, radius = w * 0.45f, center = center, style = Stroke(width = 6f))

            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.6f), radius = w * 0.1f, center = Offset(w * 0.25f, h * 0.65f))
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.6f), radius = w * 0.1f, center = Offset(w * 0.75f, h * 0.65f))

            val stemColor = Color(0xFF43A047)
            for (i in 0 until 5) {
                rotate(degrees = i * 72f, pivot = Offset(w * 0.5f, h * 0.15f)) {
                    val leaf = Path().apply {
                        moveTo(w * 0.5f, h * 0.15f)
                        quadraticTo(w * 0.45f, h * 0.05f, w * 0.5f, 0f)
                        quadraticTo(w * 0.55f, h * 0.05f, w * 0.5f, h * 0.15f)
                    }
                    drawPath(leaf, stemColor)
                    drawPath(leaf, Color.Black, style = Stroke(width = 3f))
                }
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.7f).offset(y = 10.dp),
            eyeSize = 32.dp,
            mouthSize = 40.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun CarrotCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            rotate(-15f) {
                val carrotPath = Path().apply {
                    moveTo(w * 0.3f, h * 0.1f)
                    quadraticTo(w * 0.5f, h * 0.05f, w * 0.7f, h * 0.1f)
                    quadraticTo(w * 0.85f, h * 0.5f, w * 0.6f, h * 0.95f)
                    quadraticTo(w * 0.5f, h * 1.0f, w * 0.4f, h * 0.95f)
                    quadraticTo(w * 0.15f, h * 0.5f, w * 0.3f, h * 0.1f)
                }
                drawPath(carrotPath, Color(0xFFFF9800))
                drawPath(carrotPath, Color.Black, style = Stroke(width = 5f))

                drawLine(Color.Black.copy(0.3f), Offset(w * 0.35f, h * 0.3f), Offset(w * 0.5f, h * 0.32f), strokeWidth = 3f)
                drawLine(Color.Black.copy(0.3f), Offset(w * 0.6f, h * 0.45f), Offset(w * 0.75f, h * 0.43f), strokeWidth = 3f)
                drawLine(Color.Black.copy(0.3f), Offset(w * 0.4f, h * 0.65f), Offset(w * 0.55f, h * 0.67f), strokeWidth = 3f)
            }

            val green = Color(0xFF4CAF50)
            for (i in -1..1) {
                rotate(i * 20f, Offset(w * 0.5f, h * 0.1f)) {
                    drawRect(green, Offset(w * 0.48f, 0f), Size(w * 0.04f, h * 0.15f))
                    drawCircle(green, radius = w * 0.06f, center = Offset(w * 0.5f, 0f))
                    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(w * 0.5f, 0f), style = Stroke(width = 2f))
                }
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f).offset(x = 5.dp, y = 10.dp),
            eyeSize = 24.dp,
            mouthSize = 28.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun CornCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val cornBody = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                quadraticTo(w * 0.8f, h * 0.1f, w * 0.8f, h * 0.5f)
                quadraticTo(w * 0.8f, h * 0.9f, w * 0.5f, h * 0.95f)
                quadraticTo(w * 0.2f, h * 0.9f, w * 0.2f, h * 0.5f)
                quadraticTo(w * 0.2f, h * 0.1f, w * 0.5f, h * 0.05f)
            }
            drawPath(cornBody, Color(0xFFFFD54F))
            drawPath(cornBody, Color.Black, style = Stroke(width = 5f))

            for (i in 1..5) {
                drawLine(Color.Black.copy(0.2f), Offset(w * 0.2f, h * (0.15f + i * 0.12f)), Offset(w * 0.8f, h * (0.15f + i * 0.12f)), strokeWidth = 2f)
                drawLine(Color.Black.copy(0.2f), Offset(w * (0.2f + i * 0.12f), h * 0.1f), Offset(w * (0.2f + i * 0.12f), h * 0.9f), strokeWidth = 2f)
            }

            val huskColor = Color(0xFF8BC34A)
            val leftHusk = Path().apply {
                moveTo(w * 0.35f, h * 0.95f)
                quadraticTo(w * 0.05f, h * 0.6f, w * 0.15f, h * 0.2f)
                quadraticTo(w * 0.3f, h * 0.4f, w * 0.35f, h * 0.8f)
            }
            drawPath(leftHusk, huskColor)
            drawPath(leftHusk, Color.Black, style = Stroke(width = 3f))

            val rightHusk = Path().apply {
                moveTo(w * 0.65f, h * 0.95f)
                quadraticTo(w * 0.95f, h * 0.6f, w * 0.85f, h * 0.2f)
                quadraticTo(w * 0.7f, h * 0.4f, w * 0.65f, h * 0.8f)
            }
            drawPath(rightHusk, huskColor)
            drawPath(rightHusk, Color.Black, style = Stroke(width = 3f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(y = 10.dp),
            eyeSize = 22.dp,
            mouthSize = 24.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun GarlicCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val garlicPath = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                quadraticTo(w * 0.95f, h * 0.3f, w * 0.9f, h * 0.75f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.1f, h * 0.75f)
                quadraticTo(w * 0.05f, h * 0.3f, w * 0.5f, h * 0.05f)
            }
            drawPath(garlicPath, Color(0xFFF5F5F5))
            drawPath(garlicPath, Color.Black, style = Stroke(width = 4f))

            drawLine(Color.Black.copy(0.2f), Offset(w * 0.5f, h * 0.05f), Offset(w * 0.5f, h * 0.95f), strokeWidth = 2f)
            drawLine(Color.Black.copy(0.2f), Offset(w * 0.5f, h * 0.05f), Offset(w * 0.25f, h * 0.85f), strokeWidth = 2f)
            drawLine(Color.Black.copy(0.2f), Offset(w * 0.5f, h * 0.05f), Offset(w * 0.75f, h * 0.85f), strokeWidth = 2f)
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f).offset(y = 15.dp),
            eyeSize = 20.dp,
            mouthSize = 18.dp,
            isOpenMouth = false
        )
    }
}

@Composable
private fun CucumberCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            rotate(10f) {
                val cucumberPath = Path().apply {
                    moveTo(w * 0.3f, h * 0.15f)
                    quadraticTo(w * 0.5f, h * 0.1f, w * 0.7f, h * 0.15f)
                    quadraticTo(w * 0.95f, h * 0.5f, w * 0.8f, h * 0.85f)
                    quadraticTo(w * 0.5f, h * 0.95f, w * 0.2f, h * 0.85f)
                    quadraticTo(w * 0.05f, h * 0.5f, w * 0.3f, h * 0.15f)
                }
                drawPath(cucumberPath, Color(0xFF4CAF50))
                drawPath(cucumberPath, Color.Black, style = Stroke(width = 5f))

                for (i in 0 until 12) {
                    val rx = (0.2f + Math.random().toFloat() * 0.6f) * w
                    val ry = (0.2f + Math.random().toFloat() * 0.6f) * h
                    drawCircle(Color(0xFF388E3C), radius = 6f, center = Offset(rx, ry))
                }
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(x = (-5).dp, y = 10.dp),
            eyeSize = 22.dp,
            mouthSize = 24.dp,
            isOpenMouth = false
        )
    }
}

@Composable
private fun EggplantCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            val eggplantPath = Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                cubicTo(w * 0.8f, h * 0.15f, w * 1.0f, h * 0.6f, w * 0.9f, h * 0.85f)
                quadraticTo(w * 0.5f, h * 0.95f, w * 0.1f, h * 0.85f)
                cubicTo(w * 0.0f, h * 0.6f, w * 0.2f, h * 0.15f, w * 0.5f, h * 0.15f)
            }
            drawPath(eggplantPath, Color(0xFF4A148C))
            drawPath(eggplantPath, Color.Black, style = Stroke(width = 5f))

            val capPath = Path().apply {
                moveTo(w * 0.3f, h * 0.2f)
                lineTo(w * 0.5f, 0f)
                lineTo(w * 0.7f, h * 0.2f)
                quadraticTo(w * 0.5f, h * 0.3f, w * 0.3f, h * 0.2f)
            }
            drawPath(capPath, Color(0xFF388E3C))
            drawPath(capPath, Color.Black, style = Stroke(width = 3f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.55f).offset(y = 20.dp),
            eyeSize = 20.dp,
            mouthSize = 22.dp,
            isOpenMouth = false
        )
    }
}

@Composable
private fun BroccoliCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val stemPath = Path().apply {
                moveTo(w * 0.4f, h * 0.6f)
                lineTo(w * 0.6f, h * 0.6f)
                lineTo(w * 0.7f, h * 0.95f)
                lineTo(w * 0.3f, h * 0.95f)
                close()
            }
            drawPath(stemPath, Color(0xFF81C784))
            drawPath(stemPath, Color.Black, style = Stroke(width = 3f))

            val floretColor = Color(0xFF2E7D32)
            drawCircle(floretColor, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.4f))
            drawCircle(floretColor, radius = w * 0.25f, center = Offset(w * 0.25f, h * 0.5f))
            drawCircle(floretColor, radius = w * 0.25f, center = Offset(w * 0.75f, h * 0.5f))
            
            drawCircle(Color.Black, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.4f), style = Stroke(width = 4f))
            drawCircle(Color.Black, radius = w * 0.25f, center = Offset(w * 0.25f, h * 0.5f), style = Stroke(width = 4f))
            drawCircle(Color.Black, radius = w * 0.25f, center = Offset(w * 0.75f, h * 0.5f), style = Stroke(width = 4f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f).offset(y = 20.dp),
            eyeSize = 16.dp,
            mouthSize = 18.dp,
            isOpenMouth = true
        )
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
            OpenMouth(modifier = Modifier.size(mouthSize, (mouthSize.value * 0.6).dp))
        } else {
            Mouth(modifier = Modifier.size(mouthSize, (mouthSize.value * 0.4).dp))
        }
    }
}

@Composable
private fun CharacterEye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(Color.White, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension * 0.35f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.15f,
                center = Offset(center.x + size.width * 0.15f, center.y - size.height * 0.15f)
            )
        }
    }
}

@Composable
private fun Mouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height * 1.2f, size.width, size.height * 0.2f)
        }
        drawPath(path, Color.Black, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable
private fun OpenMouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height * 1.5f, size.width, size.height * 0.2f)
            quadraticTo(size.width / 2, size.height * 0.1f, 0f, size.height * 0.2f)
            close()
        }
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, Color.Black, style = Stroke(width = 3f))
        
        val tonguePath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.8f)
            quadraticTo(size.width / 2, size.height * 1.1f, size.width * 0.75f, size.height * 0.8f)
        }
        drawPath(tonguePath, Color(0xFFFF80AB))
    }
}

@Preview(showBackground = true)
@Composable
private fun VegetablesGroupIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            VegetablesGroupIllustration()
        }
    }
}
