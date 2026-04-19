package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun FruitGroupIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.fruit_characters_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.1f)
            )
        }

        PineappleCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopStart)
                .offset(x = 55.dp, y = 35.dp)
        )
        
        PearCharacter(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 45.dp)
        )

        CherryCharacter(
            modifier = Modifier
                .fillMaxSize(0.2f)
                .align(Alignment.TopCenter)
                .offset(x = (-15).dp, y = 50.dp)
        )

        PlumCharacter(
            modifier = Modifier
                .fillMaxSize(0.25f)
                .align(Alignment.TopEnd)
                .offset(x = (-100).dp, y = 55.dp)
        )

        OrangeCharacter(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 20.dp)
        )

        AppleCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )

        BananaCharacter(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = 20.dp)
        )

        GrapesCharacter(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.BottomStart)
                .offset(x = 25.dp, y = (-20).dp)
        )

        WatermelonCharacter(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-25).dp)
        )

        StrawberryCharacter(
            modifier = Modifier
                .fillMaxSize(0.28f)
                .align(Alignment.BottomCenter)
                .offset(x = (-15).dp, y = (-10).dp)
        )

        BlueberriesCharacter(
            modifier = Modifier
                .fillMaxSize(0.25f)
                .align(Alignment.BottomEnd)
                .offset(x = (-85).dp, y = (-5).dp)
        )
    }
}

@Composable
private fun AppleCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val applePath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.3f)
                cubicTo(size.width * 0.8f, size.height * 0.2f, size.width * 1.1f, size.height * 0.5f, size.width * 0.5f, size.height * 0.95f)
                cubicTo((-size.width * 0.1f), size.height * 0.5f, size.width * 0.2f, size.height * 0.2f, size.width * 0.5f, size.height * 0.3f)
                close()
            }
            drawPath(applePath, Color(0xFFE53935))
            drawPath(applePath, Color(0xFFB71C1C), style = Stroke(width = 6f))

            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(size.width * 0.45f, size.height * 0.15f),
                size = Size(size.width * 0.1f, size.height * 0.15f)
            )
            val leafPath = Path().apply {
                moveTo(size.width * 0.55f, size.height * 0.15f)
                quadraticTo(size.width * 0.8f, size.height * 0.05f, size.width * 0.85f, size.height * 0.25f)
                quadraticTo(size.width * 0.6f, size.height * 0.3f, size.width * 0.55f, size.height * 0.15f)
            }
            drawPath(leafPath, Color(0xFF43A047))
            drawPath(leafPath, Color(0xFF1B5E20), style = Stroke(width = 3f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f).offset(y = 10.dp),
            eyeSize = 20.dp,
            mouthSize = 30.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun BananaCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(degrees = -15f) {
                val bananaPath = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.1f)
                    quadraticTo(size.width * 0.1f, size.height * 0.5f, size.width * 0.4f, size.height * 0.9f)
                    quadraticTo(size.width * 0.7f, size.height * 0.95f, size.width * 0.95f, size.height * 0.8f)
                    quadraticTo(size.width * 0.8f, size.height * 0.6f, size.width * 0.9f, size.height * 0.4f)
                    quadraticTo(size.width * 0.6f, size.height * 0.3f, size.width * 0.3f, size.height * 0.1f)
                }
                drawPath(bananaPath, Color(0xFFFFEB3B))
                drawPath(bananaPath, Color(0xFFFBC02D), style = Stroke(width = 6f))
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(x = 15.dp, y = 5.dp),
            eyeSize = 16.dp,
            mouthSize = 20.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun PineappleCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bodyPath = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.5f)
                quadraticTo(size.width * 0.2f, size.height * 0.3f, size.width * 0.5f, size.height * 0.3f)
                quadraticTo(size.width * 0.8f, size.height * 0.3f, size.width * 0.8f, size.height * 0.5f)
                quadraticTo(size.width * 0.8f, size.height * 0.9f, size.width * 0.5f, size.height * 0.95f)
                quadraticTo(size.width * 0.2f, size.height * 0.9f, size.width * 0.2f, size.height * 0.5f)
            }
            drawPath(bodyPath, Color(0xFFFFA000))
            drawPath(bodyPath, Color(0xFFE65100), style = Stroke(width = 6f))

            for (i in 1..4) {
                drawLine(Color(0xFFE65100), Offset(size.width * 0.25f, size.height * (0.3f + i * 0.12f)), Offset(size.width * 0.75f, size.height * (0.3f + i * 0.12f)), strokeWidth = 2f)
            }

            val leafColor = Color(0xFF43A047)
            for (i in -2..2) {
                rotate(degrees = i * 15f, pivot = Offset(size.width * 0.5f, size.height * 0.3f)) {
                    val leaf = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.3f)
                        quadraticTo(size.width * 0.45f, size.height * 0.1f, size.width * 0.5f, 0f)
                        quadraticTo(size.width * 0.55f, size.height * 0.1f, size.width * 0.5f, size.height * 0.3f)
                    }
                    drawPath(leaf, leafColor)
                    drawPath(leaf, Color(0xFF1B5E20), style = Stroke(width = 2f))
                }
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.4f).offset(y = 20.dp),
            eyeSize = 14.dp,
            mouthSize = 18.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun GrapesCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grapeColor = Color(0xFF7B1FA2)
            val grapePositions = listOf(
                Offset(0.3f, 0.4f), Offset(0.5f, 0.4f), Offset(0.7f, 0.4f),
                Offset(0.2f, 0.6f), Offset(0.4f, 0.6f), Offset(0.6f, 0.6f), Offset(0.8f, 0.6f),
                Offset(0.3f, 0.8f), Offset(0.5f, 0.8f), Offset(0.7f, 0.8f),
                Offset(0.5f, 0.95f)
            )
            grapePositions.forEach { pos ->
                drawCircle(grapeColor, radius = size.width * 0.12f, center = Offset(pos.x * size.width, pos.y * size.height))
                drawCircle(Color(0xFF4A148C), radius = size.width * 0.12f, center = Offset(pos.x * size.width, pos.y * size.height), style = Stroke(width = 3f))
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.45f).offset(y = (-5).dp),
            eyeSize = 18.dp,
            mouthSize = 22.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun WatermelonCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val slicePath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.5f)
                quadraticTo(size.width * 0.5f, size.height * 1.1f, size.width * 0.95f, size.height * 0.4f)
                lineTo(size.width * 0.1f, size.height * 0.5f)
                close()
            }
            drawPath(slicePath, Color(0xFF43A047))
            
            val fleshPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.52f)
                quadraticTo(size.width * 0.5f, size.height * 1.0f, size.width * 0.9f, size.height * 0.45f)
                lineTo(size.width * 0.15f, size.height * 0.52f)
                close()
            }
            drawPath(fleshPath, Color(0xFFF44336))

            val seeds = listOf(Offset(0.3f, 0.65f), Offset(0.45f, 0.75f), Offset(0.6f, 0.75f), Offset(0.75f, 0.65f))
            seeds.forEach { pos ->
                drawOval(BrandBlack, topLeft = Offset(pos.x * size.width, pos.y * size.height), size = Size(8f, 12f))
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(y = 15.dp),
            eyeSize = 16.dp,
            mouthSize = 22.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun StrawberryCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bodyPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.3f)
                quadraticTo(size.width * 0.9f, size.height * 0.3f, size.width * 0.5f, size.height * 0.95f)
                quadraticTo(size.width * 0.1f, size.height * 0.3f, size.width * 0.5f, size.height * 0.3f)
            }
            drawPath(bodyPath, Color(0xFFE53935))
            drawPath(bodyPath, Color(0xFFB71C1C), style = Stroke(width = 4f))

            for (i in 0..10) {
                val rx = (0.3f + Math.random().toFloat() * 0.4f) * size.width
                val ry = (0.4f + Math.random().toFloat() * 0.4f) * size.height
                drawCircle(Color(0xFFFFF176), radius = 3f, center = Offset(rx, ry))
            }

            val capPath = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.3f)
                lineTo(size.width * 0.5f, size.height * 0.15f)
                lineTo(size.width * 0.7f, size.height * 0.3f)
                quadraticTo(size.width * 0.5f, size.height * 0.4f, size.width * 0.3f, size.height * 0.3f)
            }
            drawPath(capPath, Color(0xFF43A047))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(y = 10.dp),
            eyeSize = 10.dp,
            mouthSize = 12.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun BlueberriesCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Blueberry(modifier = Modifier.size(40.dp).align(Alignment.CenterStart))
        Blueberry(modifier = Modifier.size(35.dp).align(Alignment.BottomEnd).offset(x = (-5).dp, y = (-5).dp))
    }
}

@Composable
private fun Blueberry(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF3F51B5))
            drawCircle(Color(0xFF1A237E), style = Stroke(width = 4f))
            drawCircle(Color(0xFF1A237E), radius = size.width * 0.2f, center = Offset(size.width * 0.5f, size.height * 0.15f), style = Stroke(width = 2f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f),
            eyeSize = 8.dp,
            mouthSize = 8.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun OrangeCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFFF9800))
            drawCircle(Color(0xFFE65100), style = Stroke(width = 6f))
            for (i in 0..15) {
                val angle = Math.random() * 2 * Math.PI
                val dist = Math.random() * size.width * 0.4f
                val x = size.width / 2 + Math.cos(angle) * dist
                val y = size.height / 2 + Math.sin(angle) * dist
                drawCircle(Color(0xFFE65100), radius = 2f, center = Offset(x.toFloat(), y.toFloat()))
            }
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f),
            eyeSize = 18.dp,
            mouthSize = 24.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun PearCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pearPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.15f)
                cubicTo(size.width * 0.8f, size.height * 0.15f, size.width * 1.0f, size.height * 0.6f, size.width * 0.9f, size.height * 0.85f)
                quadraticTo(size.width * 0.5f, size.height * 1.0f, size.width * 0.1f, size.height * 0.85f)
                cubicTo(size.width * 0.0f, size.height * 0.6f, size.width * 0.2f, size.height * 0.15f, size.width * 0.5f, size.height * 0.15f)
            }
            drawPath(pearPath, Color(0xFFFBC02D))
            drawPath(pearPath, Color(0xFFF9A825), style = Stroke(width = 5f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.5f).offset(y = 15.dp),
            eyeSize = 14.dp,
            mouthSize = 18.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun PlumCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF673AB7))
            drawCircle(Color(0xFF311B92), style = Stroke(width = 5f))
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.6f),
            eyeSize = 12.dp,
            mouthSize = 14.dp,
            isOpenMouth = true
        )
    }
}

@Composable
private fun CherryCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFD32F2F))
            drawCircle(Color(0xFFB71C1C), style = Stroke(width = 4f))
            drawLine(Color(0xFF43A047), Offset(size.width * 0.5f, size.height * 0.1f), Offset(size.width * 0.6f, (-size.height * 0.3f)), strokeWidth = 3f)
        }
        CharacterFace(
            modifier = Modifier.fillMaxSize(0.7f),
            eyeSize = 10.dp,
            mouthSize = 10.dp,
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
            .background(BrandWhite, CircleShape)
            .padding(1.dp)
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
                radius = size.minDimension * 0.12f,
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
            quadraticTo(size.width / 2, size.height, size.width, size.height * 0.2f)
        }
        drawPath(path, BrandBlack, style = Stroke(width = 3f))
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
        drawPath(path, BrandBlack, style = Stroke(width = 2f))
        
        val tonguePath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.8f)
            quadraticTo(size.width / 2, size.height * 1.0f, size.width * 0.8f, size.height * 0.8f)
        }
        drawPath(tonguePath, Color(0xFFFF80AB))
    }
}

@Preview(showBackground = true)
@Composable
private fun FruitGroupIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        FruitGroupIllustration()
    }
}
