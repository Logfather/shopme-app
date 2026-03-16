package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun RiceIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.rice_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground Shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        ) {
            drawOval(
                color = Color(0xFFF0EAD6).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.15f)
            )
        }

        // Stalks in background
        RiceStalks(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 80.dp)
        )

        // Wooden Scoop
        WoodenScoop(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopCenter)
                .offset(x = 50.dp, y = 60.dp)
                .rotate(15f)
        )

        // Rice Sack
        RiceSack(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 20.dp)
        )

        // Rice Bowl
        RiceBowl(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-10).dp)
        )

        // Scattered Grains
        ScatteredGrains(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun RiceSack(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sackPath = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.95f)
                quadraticTo(size.width * 0.1f, size.height * 0.95f, size.width * 0.1f, size.height * 0.8f)
                lineTo(size.width * 0.15f, size.height * 0.4f)
                quadraticTo(size.width * 0.15f, size.height * 0.3f, size.width * 0.3f, size.height * 0.3f)
                lineTo(size.width * 0.7f, size.height * 0.3f)
                quadraticTo(size.width * 0.85f, size.height * 0.3f, size.width * 0.85f, size.height * 0.4f)
                lineTo(size.width * 0.9f, size.height * 0.8f)
                quadraticTo(size.width * 0.9f, size.height * 0.95f, size.width * 0.8f, size.height * 0.95f)
                close()
            }
            
            val sackColor = Color(0xFFD2B48C)
            val sackDark = Color(0xFFBC8F8F)
            
            drawPath(
                path = sackPath,
                brush = Brush.verticalGradient(listOf(sackColor, sackDark))
            )
            drawPath(path = sackPath, color = Color(0xFF5D4037), style = Stroke(width = 4f))

            // Sack fold/top rim
            val rimPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.4f)
                quadraticTo(size.width * 0.15f, size.height * 0.3f, size.width * 0.3f, size.height * 0.3f)
                lineTo(size.width * 0.7f, size.height * 0.3f)
                quadraticTo(size.width * 0.85f, size.height * 0.3f, size.width * 0.85f, size.height * 0.4f)
                lineTo(size.width * 0.85f, size.height * 0.45f)
                quadraticTo(size.width * 0.5f, size.height * 0.55f, size.width * 0.15f, size.height * 0.45f)
                close()
            }
            drawPath(rimPath, sackColor)
            drawPath(rimPath, Color(0xFF5D4037), style = Stroke(width = 4f))

            // Label background
            drawRoundRect(
                color = Color(0xFFF5F5DC),
                topLeft = Offset(size.width * 0.3f, size.height * 0.5f),
                size = Size(size.width * 0.4f, size.height * 0.2f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(size.width * 0.3f, size.height * 0.5f),
                size = Size(size.width * 0.4f, size.height * 0.2f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2f)
            )
            
            // Texture lines for burlap
            for (i in 0..10) {
                val x = size.width * (0.2f + i * 0.06f)
                if (x < size.width * 0.85f) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.1f),
                        start = Offset(x, size.height * 0.45f),
                        end = Offset(x, size.height * 0.9f),
                        strokeWidth = 1f
                    )
                }
            }

            // Rice piling up inside
            drawRicePile(
                this,
                Offset(size.width * 0.5f, size.height * 0.35f),
                Size(size.width * 0.6f, size.height * 0.3f)
            )
        }

        // Label text
        Text(
            text = stringResource(R.string.reis_label),
            color = Color(0xFF5D4037),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = 25.dp)
        )

        // Face
        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .offset(y = 60.dp)
        ) {
            CharacterFace(
                modifier = Modifier.align(Alignment.Center),
                eyeSize = 28.dp,
                mouthSize = 24.dp
            )
        }
    }
}

@Composable
private fun RiceBowl(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Bowl shape
            val bowlPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.5f)
                quadraticTo(size.width * 0.1f, size.height * 0.9f, size.width * 0.5f, size.height * 0.9f)
                quadraticTo(size.width * 0.9f, size.height * 0.9f, size.width * 0.9f, size.height * 0.5f)
                close()
            }
            
            val woodColor = Color(0xFF8B4513)
            val woodLight = Color(0xFFA0522D)
            
            drawPath(
                path = bowlPath,
                brush = Brush.verticalGradient(listOf(woodLight, woodColor))
            )
            drawPath(path = bowlPath, color = Color(0xFF3E2723), style = Stroke(width = 4f))

            // Rice in bowl
            drawRicePile(
                this,
                Offset(size.width * 0.5f, size.height * 0.5f),
                Size(size.width * 0.7f, size.height * 0.4f)
            )
        }

        // Face
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .offset(y = 25.dp)
        ) {
            CharacterFace(
                modifier = Modifier.align(Alignment.Center),
                eyeSize = 24.dp,
                mouthSize = 20.dp
            )
        }
    }
}

@Composable
private fun WoodenScoop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scoopColor = Color(0xFFCD853F)
        val scoopPath = Path().apply {
            // Handle
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(size.width * 0.4f, 0f),
                        size = Size(size.width * 0.2f, size.height * 0.6f)
                    ),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            )
            // Scoop head
            addOval(
                Rect(
                    center = Offset(size.width * 0.5f, size.height * 0.7f),
                    radius = size.width * 0.35f
                )
            )
        }
        drawPath(scoopPath, scoopColor)
        drawPath(scoopPath, Color(0xFF5D4037), style = Stroke(width = 4f))
        
        // Scoop inner shadow
        drawArc(
            color = Color.Black.copy(alpha = 0.1f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.55f),
            size = Size(size.width * 0.5f, size.height * 0.3f)
        )
    }
}

@Composable
private fun RiceStalks(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stalkColor = Color(0xFFDAA520)
        
        fun drawStalk(offset: Offset) {
            val stalkPath = Path().apply {
                moveTo(offset.x, offset.y)
                quadraticTo(offset.x + 20f, offset.y - 100f, offset.x - 30f, offset.y - 200f)
            }
            drawPath(stalkPath, stalkColor, style = Stroke(width = 3f))
            
            // Grain heads
            for (i in 0..8) {
                val progress = i / 8f
                val grainX = offset.x + (20f * (1 - progress)) - (30f * progress)
                val grainY = offset.y - (200f * progress)
                drawOval(
                    color = stalkColor,
                    topLeft = Offset(grainX - 8f, grainY - 12f),
                    size = Size(16f, 24f)
                )
            }
        }
        
        drawStalk(Offset(size.width * 0.8f, size.height))
        drawStalk(Offset(size.width * 0.6f, size.height))
    }
}

@Composable
private fun ScatteredGrains(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val grainColor = Color.White
        val grainBorder = Color.Black.copy(alpha = 0.2f)
        
        fun drawGrain(x: Float, y: Float) {
            val grainPath = Path().apply {
                addOval(Rect(center = Offset(x, y), radius = 6f))
            }
            drawPath(grainPath, grainColor)
            drawPath(grainPath, grainBorder, style = Stroke(width = 1f))
        }

        drawGrain(size.width * 0.1f, size.height * 0.8f)
        drawGrain(size.width * 0.15f, size.height * 0.85f)
        drawGrain(size.width * 0.25f, size.height * 0.88f)
        drawGrain(size.width * 0.05f, size.height * 0.78f)
        
        drawGrain(size.width * 0.8f, size.height * 0.92f)
        drawGrain(size.width * 0.85f, size.height * 0.95f)
    }
}

private fun drawRicePile(drawScope: DrawScope, center: Offset, size: Size) {
    val grainColor = Color.White
    val grainBorder = Color(0xFFE0E0E0)
    
    // Base shape for the pile
    drawScope.drawOval(
        color = grainColor,
        topLeft = Offset(center.x - size.width / 2, center.y - size.height / 2),
        size = size
    )
    
    // Draw individual grains on top for texture
    val random = java.util.Random(42)
    for (i in 0..30) {
        val dx = (random.nextFloat() - 0.5f) * size.width * 0.8f
        val dy = (random.nextFloat() - 0.5f) * size.height * 0.6f
        drawScope.drawOval(
            color = grainColor,
            topLeft = Offset(center.x + dx - 6f, center.y + dy - 10f),
            size = Size(12f, 20f)
        )
        drawScope.drawOval(
            color = grainBorder,
            topLeft = Offset(center.x + dx - 6f, center.y + dy - 10f),
            size = Size(12f, 20f),
            style = Stroke(width = 1f)
        )
    }
}

@Composable
private fun CharacterFace(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 24.dp,
    mouthSize: Dp = 20.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CharacterEye(size = eyeSize)
            CharacterEye(size = eyeSize)
        }
        Spacer(modifier = Modifier.size(4.dp))
        Mouth(size = mouthSize)
        
        // Rosy Cheeks
        Canvas(modifier = Modifier.size(60.dp, 10.dp)) {
            drawCircle(
                color = Color(0xFFFFA07A).copy(alpha = 0.6f),
                radius = 15f,
                center = Offset(10f, 5f)
            )
            drawCircle(
                color = Color(0xFFFFA07A).copy(alpha = 0.6f),
                radius = 15f,
                center = Offset(size.width - 10f, 5f)
            )
        }
    }
}

@Composable
private fun CharacterEye(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.Black, radius = size.toPx() * 0.35f, center = center)
            drawCircle(
                color = Color.White,
                radius = size.toPx() * 0.1f,
                center = Offset(center.x + size.toPx() * 0.15f, center.y - size.toPx() * 0.15f)
            )
        }
        // Outline
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.Black, radius = size.toPx() / 2f, style = Stroke(width = 2f))
        }
    }
}

@Composable
private fun Mouth(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            quadraticTo(size.toPx() / 2f, size.toPx(), size.toPx(), 0f)
            close()
        }
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, Color.Black, style = Stroke(width = 3f))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(size.toPx() * 0.3f, size.toPx() * 0.6f)
            quadraticTo(size.toPx() / 2f, size.toPx() * 0.9f, size.toPx() * 0.7f, size.toPx() * 0.6f)
        }
        drawPath(tonguePath, Color(0xFFFF69B4))
    }
}

@Preview(showBackground = true)
@Composable
private fun RiceIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            RiceIllustration()
        }
    }
}
