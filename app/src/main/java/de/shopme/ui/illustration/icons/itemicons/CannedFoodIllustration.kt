package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite

@Composable
fun CannedFoodIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.canned_food_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Shadow/Ground
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = Color(0xFFE5E1C3).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
                size = Size(size.width * 0.8f, size.height * 0.2f)
            )
        }

        // Tuna Can (Back Right)
        Can(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 40.dp),
            baseColor = Color(0xFF2196F3),
            labelColor = Color(0xFFB3E5FC),
            content = { TunaLabel(Modifier.fillMaxSize()) },
            canContent = { TunaContent(Modifier.fillMaxSize()) },
            lidRotation = 10f,
            lidOffset = Offset(10f, -40f)
        )

        // Peas Can (Back Left)
        Can(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 60.dp),
            baseColor = Color(0xFF4CAF50),
            labelColor = Color(0xFFDCEDC8),
            content = { PeasLabel(Modifier.fillMaxSize()) },
            canContent = { PeasContent(Modifier.fillMaxSize()) },
            lidRotation = -15f,
            lidOffset = Offset(-10f, -30f)
        )

        // Tomato Can (Front Center)
        Can(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp),
            baseColor = Color(0xFFF44336),
            labelColor = Color(0xFFFFCCBC),
            content = { TomatoLabel(Modifier.fillMaxSize()) },
            isFront = true
        )

        // Sparkles and dots
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSparkle(Offset(size.width * 0.35f, size.height * 0.15f), 12f)
            drawSparkle(Offset(size.width * 0.45f, size.height * 0.1f), 18f)
            drawSparkle(Offset(size.width * 0.92f, size.height * 0.5f), 15f)
            
            drawCircle(Color(0xFFFFD54F), radius = 4f, center = Offset(size.width * 0.15f, size.height * 0.85f))
            drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(size.width * 0.3f, size.height * 0.9f))
            drawCircle(Color(0xFFFFD54F), radius = 3f, center = Offset(size.width * 0.8f, size.height * 0.8f))
        }
    }
}

@Composable
private fun Can(
    modifier: Modifier = Modifier,
    baseColor: Color,
    labelColor: Color,
    content: @Composable () -> Unit = {},
    canContent: @Composable () -> Unit = {},
    lidRotation: Float = 0f,
    lidOffset: Offset = Offset.Zero,
    isFront: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        // Can Lid (Open)
        Lid(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(2f)
                .align(Alignment.TopCenter)
                .offset(lidOffset.x.dp, lidOffset.y.dp)
                .rotate(lidRotation)
        )

        // Can Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(baseColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .border(3.dp, Color(0xFF424242), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        ) {
            // Can top rim/opening
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.TopCenter)) {
                drawOval(
                    color = Color(0xFF424242),
                    style = Stroke(width = 6f)
                )
                drawOval(
                    color = Color(0xFFBDBDBD)
                )
            }

            // Inner content (if open)
            Box(modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(16.dp)
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)
                .clip(CircleShape)) {
                canContent()
            }

            // Label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.Center)
                    .background(labelColor)
                    .customBorder(width = 2.dp, color = Color(0xFF424242))
            ) {
                content()
            }

            // Face
            Face(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                isFront = isFront
            )
        }

        // Bottom Rim
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .align(Alignment.BottomCenter)) {
            drawRoundRect(
                color = Color(0xFFBDBDBD),
                size = Size(size.width, 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF424242),
                size = Size(size.width, 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
private fun Lid(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Lid body
            drawOval(
                color = Color(0xFFBDBDBD)
            )
            drawOval(
                color = Color(0xFF424242),
                style = Stroke(width = 4f)
            )
            
            // Lid rings
            drawOval(
                color = Color(0xFF757575),
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                style = Stroke(width = 2f)
            )

            // Pull tab
            drawCircle(
                color = Color(0xFF424242),
                radius = 8f,
                center = Offset(size.width * 0.5f, size.height * 0.85f),
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
private fun Face(
    modifier: Modifier = Modifier,
    isFront: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(if (isFront) 8.dp else 4.dp)) {
                Eye(eyeSize = if (isFront) 32.dp else 24.dp)
                Eye(eyeSize = if (isFront) 32.dp else 24.dp)
            }
            Spacer(modifier = Modifier.height(if (isFront) 4.dp else 2.dp))
            Mouth(modifier = Modifier.size(if (isFront) 36.dp else 24.dp))
        }
    }
}

@Composable
private fun Eye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(BrandWhite, CircleShape)
            .border(2.dp, Color(0xFF424242), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBlack,
                radius = eyeSize.toPx() * 0.25f,
                center = center.plus(Offset(2f, 2f))
            )
            drawCircle(
                color = BrandWhite,
                radius = eyeSize.toPx() * 0.08f,
                center = center.minus(Offset(eyeSize.toPx() * 0.1f, eyeSize.toPx() * 0.1f))
            )
        }
    }
}

@Composable
private fun Mouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.3f)
            quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.9f, size.height * 0.3f)
            close()
        }
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, Color(0xFF424242), style = Stroke(width = 3f))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.65f)
            quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.7f, size.height * 0.65f)
        }
        drawPath(tonguePath, Color(0xFFFF80AB))
    }
}

@Composable
private fun TunaLabel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "TUNA",
            color = Color(0xFFFBC02D),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(y = (-2).dp)
        )
    }
}

@Composable
private fun TunaContent(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFFFFCDD2))
        for (i in 0..5) {
            drawOval(
                color = Color(0xFFEF9A9A),
                topLeft = Offset(i * 15f, 5f),
                size = Size(20f, 10f)
            )
        }
    }
}

@Composable
private fun PeasLabel(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val podPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.7f)
                quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.9f, size.height * 0.6f)
                quadraticTo(size.width * 0.5f, size.height * 0.75f, size.width * 0.1f, size.height * 0.7f)
            }
            drawPath(podPath, Color(0xFF388E3C))
            drawPath(podPath, Color(0xFF1B5E20), style = Stroke(width = 2f))
            
            // Little peas in pod
            for (i in 0..3) {
                drawCircle(
                    color = Color(0xFF81C784),
                    radius = 4f,
                    center = Offset(size.width * (0.25f + i * 0.15f), size.height * 0.78f)
                )
            }
        }
    }
}

@Composable
private fun PeasContent(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFF2E7D32))
        for (i in 0..8) {
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 6f,
                center = Offset(i * 12f + 5f, 8f)
            )
        }
    }
}

@Composable
private fun TomatoLabel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize(0.8f)) {
            drawCircle(
                color = Color(0xFFF44336),
                radius = size.minDimension / 2.5f,
                center = center
            )
            drawCircle(
                color = Color(0xFFB71C1C),
                radius = size.minDimension / 2.5f,
                center = center,
                style = Stroke(width = 2f)
            )
            // Stem
            val stemPath = Path().apply {
                moveTo(center.x, center.y - 15f)
                lineTo(center.x - 5f, center.y - 25f)
                lineTo(center.x + 5f, center.y - 25f)
                close()
            }
            drawPath(stemPath, Color(0xFF4CAF50))
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
    }
    drawPath(path, Color(0xFFFFD54F))
}

private fun Modifier.customBorder(width: Dp, color: Color): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = width.toPx()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
)

@Preview(showBackground = true)
@Composable
private fun CannedFoodIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CannedFoodIllustration()
    }
}
