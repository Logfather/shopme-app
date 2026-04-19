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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
@Composable
fun FrozenFoodIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.frozen_food_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        CoolerBagBack(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )
        FishSticksBox(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 40.dp)
                .rotate(-10f)
        )
        PeasBag(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-80).dp, y = 60.dp)
                .rotate(5f)
        )
        IceCreamTub(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 100.dp)
                .rotate(10f)
        )
        ShrimpBag(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 20.dp)
                .rotate(-15f)
        )
        PizzaBag(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.Center)
                .offset(x = 10.dp, y = 40.dp)
        )
        CoolerBagFront(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )
        Popsicle(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-60).dp, y = (-20).dp)
                .rotate(20f)
        )
        IceCube(modifier = Modifier.size(50.dp).align(Alignment.BottomStart).offset(x = 40.dp, y = (-40).dp).rotate(-15f))
        IceCube(modifier = Modifier.size(45.dp).align(Alignment.BottomStart).offset(x = 80.dp, y = (-20).dp).rotate(10f))
        IceCube(modifier = Modifier.size(55.dp).align(Alignment.BottomEnd).offset(x = (-20).dp, y = (-60).dp).rotate(5f))
        Bubble(modifier = Modifier.size(12.dp).align(Alignment.TopStart).offset(x = 50.dp, y = 100.dp))
        Bubble(modifier = Modifier.size(10.dp).align(Alignment.TopEnd).offset(x = (-100).dp, y = 40.dp))
        Bubble(modifier = Modifier.size(15.dp).align(Alignment.TopEnd).offset(x = (-40).dp, y = 80.dp))
    }
}
@Composable
private fun CoolerBagBack(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.05f, size.height * 0.4f)
            lineTo(size.width * 0.95f, size.height * 0.4f)
            lineTo(size.width * 0.95f, size.height * 0.85f)
            quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.05f, size.height * 0.85f)
            close()
        }
        drawPath(path, Color(0xFF0288D1))
        drawPath(path, Color(0xFF01579B), style = Stroke(width = 4f))
    }
}
@Composable
private fun CoolerBagFront(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.02f, size.height * 0.45f)
            lineTo(size.width * 0.98f, size.height * 0.45f)
            lineTo(size.width * 0.95f, size.height * 0.85f)
            quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width * 0.05f, size.height * 0.85f)
            close()
        }
        drawPath(path, Color(0xFF29B6F6))
        drawPath(path, Color(0xFF01579B), style = Stroke(width = 6f))
        val frostPath = Path().apply {
            moveTo(size.width * 0.02f, size.height * 0.45f)
            for (i in 0 until 10) {
                val x = size.width * (0.02f + i * 0.1f)
                val y = size.height * (0.45f + (if (i % 2 == 0) 0.05f else 0.1f))
                quadraticTo(x + size.width * 0.05f, y + size.height * 0.05f, x + size.width * 0.1f, size.height * 0.45f)
            }
        }
        drawPath(frostPath, BrandWhite)
        drawPath(frostPath, Color(0xFFB3E5FC), style = Stroke(width = 2f))
        val centerX = size.width * 0.25f
        val centerY = size.height * 0.7f
        val flakeSize = 60f
        for (i in 0 until 6) {
            val angle = i * 60f
            val x = centerX + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * flakeSize
            val y = centerY + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * flakeSize
            drawLine(BrandWhite, Offset(centerX, centerY), Offset(x, y), strokeWidth = 8f, cap = StrokeCap.Round)
            val xSub = centerX + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * flakeSize * 0.6f
            val ySub = centerY + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * flakeSize * 0.6f
            drawLine(BrandWhite, Offset(xSub, ySub), Offset(xSub + 10f, ySub + 10f), strokeWidth = 4f)
            drawLine(BrandWhite, Offset(xSub, ySub), Offset(xSub - 10f, ySub - 10f), strokeWidth = 4f)
        }
    }
}
@Composable
private fun FishSticksBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFF039BE5),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = BrandBlack,
                style = Stroke(width = 4f),
                cornerRadius = CornerRadius(20f, 20f)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.fish_sticks_label).substringBefore(" "),
                style = TextStyle(color = Color(0xFFFF9100), fontWeight = FontWeight.Black, fontSize = 24.sp)
            )
            Text(
                text = stringResource(R.string.fish_sticks_label).substringAfter(" "),
                style = TextStyle(color = Color(0xFFFF9100), fontWeight = FontWeight.Black, fontSize = 20.sp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Face(modifier = Modifier.size(60.dp))
        }
    }
}
@Composable
private fun PeasBag(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.1f)
                lineTo(size.width * 0.9f, size.height * 0.1f)
                lineTo(size.width * 0.95f, size.height * 0.9f)
                lineTo(size.width * 0.05f, size.height * 0.9f)
                close()
            }
            drawPath(path, Color(0xFF43A047))
            drawPath(path, BrandBlack, style = Stroke(width = 4f))

            for (i in 0 until 15) {
                drawCircle(
                    color = Color(0xFF2E7D32).copy(alpha = 0.6f),
                    radius = 8f,
                    center = Offset(size.width * (0.2f + (i % 5) * 0.15f), size.height * (0.2f + (i / 5) * 0.2f))
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.peas_label),
                style = TextStyle(color = BrandWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Face(modifier = Modifier.size(50.dp))
        }
    }
}
@Composable
private fun ShrimpBag(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = BrandWhite.copy(alpha = 0.4f),
                cornerRadius = CornerRadius(15f, 15f)
            )
            drawRoundRect(
                color = BrandBlack,
                style = Stroke(width = 4f),
                cornerRadius = CornerRadius(15f, 15f)
            )
            for (i in 0 until 5) {
                drawArc(
                    color = Color(0xFFFF8A80),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * (0.2f + (i % 2) * 0.4f), size.height * (0.4f + (i / 2) * 0.2f)),
                    size = Size(40f, 30f)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.shrimp_label),
                style = TextStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Black, fontSize = 20.sp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Face(modifier = Modifier.size(60.dp))
        }
    }
}
@Composable
private fun PizzaBag(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFFFF176), radius = size.minDimension * 0.45f)
            drawCircle(BrandBlack, radius = size.minDimension * 0.45f, style = Stroke(width = 4f))

            for (i in 0 until 8) {
                drawCircle(
                    color = Color(0xFFFF9100),
                    radius = 10f,
                    center = Offset(size.width / 2 + (i % 3 - 1) * 30f, size.height / 2 + (i / 3 - 1) * 30f)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.pizza_label),
                style = TextStyle(color = Color(0xFFE65100), fontWeight = FontWeight.Black, fontSize = 26.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Face(modifier = Modifier.size(70.dp))
        }
    }
}
@Composable
private fun IceCreamTub(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.3f)
                lineTo(size.width * 0.8f, size.height * 0.3f)
                lineTo(size.width * 0.75f, size.height * 0.9f)
                lineTo(size.width * 0.25f, size.height * 0.9f)
                close()
            }
            drawPath(path, Color(0xFFF06292))
            drawPath(path, BrandBlack, style = Stroke(width = 4f))
            drawArc(
                color = Color(0xFFFFF9C4),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(size.width * 0.15f, size.height * 0.1f),
                size = Size(size.width * 0.7f, size.height * 0.4f)
            )
            drawArc(
                color = Color(0xFF5D4037),
                startAngle = 180f,
                sweepAngle = 100f,
                useCenter = true,
                topLeft = Offset(size.width * 0.3f, size.height * 0.05f),
                size = Size(size.width * 0.5f, size.height * 0.3f)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ice_cream_label),
                style = TextStyle(color = BrandWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Face(modifier = Modifier.size(50.dp).offset(y = (-10).dp))
        }
    }
}
@Composable
private fun Popsicle(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stickWidth = size.width * 0.15f
            val stickHeight = size.height * 0.3f
            drawRoundRect(
                color = Color(0xFFD7CCC8),
                topLeft = Offset(size.width / 2 - stickWidth / 2, size.height * 0.7f),
                size = Size(stickWidth, stickHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )

            drawRoundRect(
                color = Color(0xFFE53935),
                topLeft = Offset(size.width * 0.2f, size.height * 0.1f),
                size = Size(size.width * 0.6f, size.height * 0.25f),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawRect(
                color = BrandWhite,
                topLeft = Offset(size.width * 0.2f, size.height * 0.35f),
                size = Size(size.width * 0.6f, size.height * 0.15f)
            )
            drawRoundRect(
                color = Color(0xFF1E88E5),
                topLeft = Offset(size.width * 0.2f, size.height * 0.5f),
                size = Size(size.width * 0.6f, size.height * 0.25f),
                cornerRadius = CornerRadius(0f, 0f)
            )

            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(size.width * 0.2f, size.height * 0.1f),
                size = Size(size.width * 0.6f, size.height * 0.65f),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(width = 4f)
            )
        }
        Face(modifier = Modifier.size(50.dp).align(Alignment.Center).offset(y = (-10).dp))
    }
}
@Composable
private fun IceCube(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0xFFB3E5FC).copy(alpha = 0.7f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Color(0xFF0288D1).copy(alpha = 0.5f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 2f)
        )
    }
}
@Composable
private fun Bubble(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = Color(0xFFB3E5FC),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = BrandWhite.copy(alpha = 0.8f),
            radius = size.minDimension / 4f,
            center = Offset(size.width * 0.3f, size.height * 0.3f)
        )
    }
}
@Composable
private fun Face(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Eye(modifier = Modifier.size(24.dp))
            Eye(modifier = Modifier.size(24.dp))
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val mouthPath = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.7f)
                quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.65f, size.height * 0.7f)
                close()
            }
            drawPath(mouthPath, Color(0xFFB71C1C))
            drawPath(mouthPath, BrandBlack, style = Stroke(width = 2f))
        }
    }
}
@Composable
private fun Eye(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(BrandWhite, CircleShape)
            .border(2.dp, BrandBlack, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBlack,
                radius = size.minDimension / 4f,
                center = center
            )
            drawCircle(
                color = BrandWhite,
                radius = size.minDimension / 10f,
                center = Offset(center.x + 2f, center.y - 2f)
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun FrozenFoodIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        FrozenFoodIllustration()
    }
}
