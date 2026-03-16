package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun ReadyMealIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.ready_meal_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Splashes(modifier = Modifier.fillMaxSize())

        NoodleCup(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 20.dp)
        )

        PizzaBox(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 40.dp)
                .rotate(5f)
        )

        MealTray(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        )

        Parsley(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = 10.dp)
                .rotate(-15f)
        )
    }
}

@Composable
private fun Splashes(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawSplash(Offset(size.width * 0.45f, size.height * 0.15f), 15f, 40f, -20f)
        drawSplash(Offset(size.width * 0.5f, size.height * 0.18f), 12f, 30f, 10f)
        drawSplash(Offset(size.width * 0.1f, size.height * 0.5f), 18f, 45f, 30f)
        drawSplash(Offset(size.width * 0.08f, size.height * 0.55f), 14f, 35f, 50f)
    }
}

private fun DrawScope.drawSplash(
    center: Offset,
    width: Float,
    height: Float,
    rotation: Float
) {
    rotate(rotation, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - height / 2)
            quadraticTo(center.x + width / 2, center.y, center.x, center.y + height / 2)
            quadraticTo(center.x - width / 2, center.y, center.x, center.y - height / 2)
        }
        drawPath(path, Color(0xFFFF9800))
    }
}

@Composable
private fun NoodleCup(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cupPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.25f)
                lineTo(size.width * 0.9f, size.height * 0.25f)
                lineTo(size.width * 0.85f, size.height * 0.9f)
                lineTo(size.width * 0.15f, size.height * 0.9f)
                close()
            }
            drawPath(cupPath, Color.White)
            drawPath(cupPath, Color.Black, style = Stroke(width = 4f))

            clipPath(cupPath) {
                drawRect(Color(0xFFFFD54F), topLeft = Offset(0f, size.height * 0.4f), size = Size(size.width, size.height * 0.1f))
                drawRect(Color(0xFFE53935), topLeft = Offset(0f, size.height * 0.5f), size = Size(size.width, size.height * 0.2f))
                drawRect(Color(0xFFFFD54F), topLeft = Offset(0f, size.height * 0.7f), size = Size(size.width, size.height * 0.1f))
            }

            drawArc(
                color = Color(0xFFFFD54F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(size.width * 0.12f, size.height * 0.15f),
                size = Size(size.width * 0.76f, size.height * 0.2f)
            )
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(size.width * 0.12f, size.height * 0.15f),
                size = Size(size.width * 0.76f, size.height * 0.2f),
                style = Stroke(width = 4f)
            )

            val chopstick1 = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.3f)
                lineTo(size.width * 0.05f, 0f)
                lineTo(size.width * 0.15f, 0f)
                lineTo(size.width * 0.2f, size.height * 0.3f)
                close()
            }
            drawPath(chopstick1, Color(0xFF795548))
            drawPath(chopstick1, Color.Black, style = Stroke(width = 3f))

            val chopstick2 = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.3f)
                lineTo(size.width * 0.2f, 0f)
                lineTo(size.width * 0.3f, 0f)
                lineTo(size.width * 0.35f, size.height * 0.3f)
                close()
            }
            drawPath(chopstick2, Color(0xFF795548))
            drawPath(chopstick2, Color.Black, style = Stroke(width = 3f))

            drawCircle(Color.White, radius = 20f, center = Offset(size.width * 0.7f, size.height * 0.25f))
            drawCircle(Color.Black, radius = 20f, center = Offset(size.width * 0.7f, size.height * 0.25f), style = Stroke(width = 2f))
            drawCircle(Color(0xFFFFD54F), radius = 10f, center = Offset(size.width * 0.7f, size.height * 0.25f))

            val steamPath = Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.1f)
                quadraticTo(size.width * 0.35f, size.height * 0.05f, size.width * 0.4f, 0f)
                moveTo(size.width * 0.5f, size.height * 0.15f)
                quadraticTo(size.width * 0.55f, size.height * 0.08f, size.width * 0.5f, 0f)
            }
            drawPath(steamPath, Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 4f, cap = StrokeCap.Round))
        }
        ReadyMealFace(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )
    }
}

@Composable
private fun PizzaBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFF1976D2),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color.Black,
                style = Stroke(width = 4f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            drawCircle(
                color = Color(0xFFFFD54F),
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.5f, size.height * 0.6f)
            )
            drawCircle(
                color = Color.Black,
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.5f, size.height * 0.6f),
                style = Stroke(width = 3f)
            )

            val centers = listOf(
                Offset(size.width * 0.4f, size.height * 0.45f),
                Offset(size.width * 0.65f, size.height * 0.48f),
                Offset(size.width * 0.8f, size.height * 0.65f),
                Offset(size.width * 0.75f, size.height * 0.8f),
                Offset(size.width * 0.5f, size.height * 0.85f),
                Offset(size.width * 0.3f, size.height * 0.75f),
                Offset(size.width * 0.25f, size.height * 0.55f)
            )
            centers.forEach { center ->
                drawCircle(Color(0xFFC62828), radius = 12f, center = center)
            }

            drawCircle(Color.White, radius = 25f, center = Offset(size.width * 0.85f, size.height * 0.15f))
            drawCircle(Color(0xFF1976D2), radius = 25f, center = Offset(size.width * 0.85f, size.height * 0.15f), style = Stroke(width = 2f))
        }
        Text(
            text = "PIZZA",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 15.dp),
            style = TextStyle(
                color = Color(0xFFE53935),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        )
        ReadyMealFace(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.Center)
                .offset(y = 15.dp)
        )
    }
}

@Composable
private fun MealTray(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trayPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = Rect(Offset(size.width * 0.05f, size.height * 0.4f), Size(size.width * 0.9f, size.height * 0.55f)),
                        cornerRadius = CornerRadius(30f, 30f)
                    )
                )
            }
            drawPath(trayPath, Color(0xFF212121))
            drawPath(trayPath, Color.Black, style = Stroke(width = 8f))

            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(size.width * 0.1f, size.height * 0.45f),
                size = Size(size.width * 0.38f, size.height * 0.25f),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(size.width * 0.1f, size.height * 0.73f),
                size = Size(size.width * 0.38f, size.height * 0.17f),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(size.width * 0.52f, size.height * 0.45f),
                size = Size(size.width * 0.38f, size.height * 0.45f),
                cornerRadius = CornerRadius(20f, 20f)
            )
        }

        MashedPotatoes(
            modifier = Modifier
                .size(120.dp, 80.dp)
                .align(Alignment.TopStart)
                .offset(x = 35.dp, y = 145.dp)
        )

        Carrots(
            modifier = Modifier
                .size(120.dp, 50.dp)
                .align(Alignment.BottomStart)
                .offset(x = 35.dp, y = (-40).dp)
        )

        Meatballs(
            modifier = Modifier
                .size(130.dp, 150.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 145.dp)
        )

        Fork(
            modifier = Modifier
                .size(60.dp, 120.dp)
                .align(Alignment.TopCenter)
                .offset(x = (-20).dp, y = 80.dp)
                .rotate(-10f)
        )
    }
}

@Composable
private fun MashedPotatoes(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(Color(0xFFFFF9C4))
            
            val gravyPath = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.3f)
                quadraticTo(size.width * 0.5f, size.height * 0.2f, size.width * 0.7f, size.height * 0.4f)
                quadraticTo(size.width * 0.8f, size.height * 0.7f, size.width * 0.5f, size.height * 0.8f)
                quadraticTo(size.width * 0.2f, size.height * 0.6f, size.width * 0.3f, size.height * 0.3f)
                close()
            }
            drawPath(gravyPath, Color(0xFF795548))
        }
        ReadyMealFace(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center),
            isGravy = true
        )
    }
}

@Composable
private fun Carrots(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val spacing = size.width / 3
        for (i in 0 until 3) {
            drawCircle(
                color = Color(0xFFFF9800),
                radius = 15f,
                center = Offset(spacing * i + 20f, size.height / 2)
            )
            drawCircle(
                color = Color.Black,
                radius = 15f,
                center = Offset(spacing * i + 20f, size.height / 2),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun Meatballs(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(Color(0xFF5D4037).copy(alpha = 0.8f), cornerRadius = CornerRadius(20f, 20f))

            val meatballs = listOf(
                Offset(size.width * 0.3f, size.height * 0.3f),
                Offset(size.width * 0.7f, size.height * 0.35f),
                Offset(size.width * 0.45f, size.height * 0.6f),
                Offset(size.width * 0.75f, size.height * 0.75f),
                Offset(size.width * 0.25f, size.height * 0.85f)
            )
            meatballs.forEach { center ->
                drawCircle(Color(0xFF4E342E), radius = 25f, center = center)
                drawCircle(Color.Black, radius = 25f, center = center, style = Stroke(width = 2f))
            }
        }
        ReadyMealFace(
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun Fork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val handlePath = Path().apply {
            moveTo(size.width * 0.4f, 0f)
            lineTo(size.width * 0.6f, 0f)
            lineTo(size.width * 0.65f, size.height * 0.7f)
            lineTo(size.width * 0.35f, size.height * 0.7f)
            close()
        }
        drawPath(handlePath, Color(0xFFBDBDBD))
        drawPath(handlePath, Color.Black, style = Stroke(width = 3f))

        val headPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.7f)
            lineTo(size.width * 0.8f, size.height * 0.7f)
            lineTo(size.width * 0.75f, size.height)
            lineTo(size.width * 0.25f, size.height)
            close()
        }
        drawPath(headPath, Color(0xFFE0E0E0))
        drawPath(headPath, Color.Black, style = Stroke(width = 3f))

        for (i in 1..3) {
            val x = size.width * (0.2f + i * 0.15f)
            drawLine(Color.Black, Offset(x, size.height * 0.8f), Offset(x, size.height), strokeWidth = 2f)
        }
    }
}

@Composable
private fun Parsley(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stem = Path().apply {
            moveTo(size.width * 0.5f, size.height)
            quadraticTo(size.width * 0.4f, size.height * 0.5f, size.width * 0.1f, size.height * 0.2f)
        }
        drawPath(stem, Color(0xFF388E3C), style = Stroke(width = 6f, cap = StrokeCap.Round))

        drawCircle(Color(0xFF4CAF50), radius = 15f, center = Offset(size.width * 0.2f, size.height * 0.3f))
        drawCircle(Color(0xFF4CAF50), radius = 18f, center = Offset(size.width * 0.4f, size.height * 0.5f))
        drawCircle(Color(0xFF4CAF50), radius = 12f, center = Offset(size.width * 0.6f, size.height * 0.7f))
    }
}

@Composable
private fun ReadyMealFace(
    modifier: Modifier = Modifier,
    isGravy: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isGravy) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val eyeY = size.height * 0.4f
                drawArc(
                    color = Color.Black,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.2f, eyeY),
                    size = Size(size.width * 0.2f, size.height * 0.15f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.Black,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.6f, eyeY),
                    size = Size(size.width * 0.2f, size.height * 0.15f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.35f, size.height * 0.6f),
                    size = Size(size.width * 0.3f, size.height * 0.15f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }
        } else {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eye(modifier = Modifier.size(24.dp))
                Eye(modifier = Modifier.size(24.dp))
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mouthPath = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.65f)
                    quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.7f, size.height * 0.65f)
                    close()
                }
                drawPath(mouthPath, Color(0xFFB71C1C))
                drawPath(mouthPath, Color.Black, style = Stroke(width = 2f))
            }
        }
    }
}

@Composable
private fun Eye(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, CircleShape)
            .border(2.dp, Color.Black, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 4f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 10f,
                center = Offset(center.x + 2f, center.y - 2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadyMealIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ReadyMealIllustration()
        }
    }
}
