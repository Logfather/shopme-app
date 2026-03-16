package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun SaucesIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.sauces_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Ground shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
        ) {
            drawOval(
                color = Color(0xFFE5E1C3).copy(alpha = 0.6f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.85f),
                size = Size(size.width * 0.8f, size.height * 0.15f)
            )
        }

        // Ketchup Bottle (Back Left)
        SauceBottle(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-40).dp),
            bottleColor = Color(0xFFD32F2F),
            capColor = Color(0xFFB71C1C),
            labelContent = {
                TomatoIcon(Modifier.size(40.dp))
            }
        )

        // Mustard Bottle (Middle)
        SauceBottle(
            modifier = Modifier
                .fillMaxSize(0.7f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp, x = (-20).dp),
            bottleColor = Color(0xFFFFD54F),
            capColor = Color(0xFFFBC02D),
            labelContent = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.mustard_label),
                        color = Color(0xFFB71C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    MustardSplat(Modifier.size(35.dp))
                }
            }
        )

        // Hot Sauce Bottle (Front Right)
        SauceBottle(
            modifier = Modifier
                .fillMaxSize(0.62f)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp),
            bottleColor = Color(0xFFF4511E),
            capColor = Color(0xFF43A047),
            labelContent = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.hot_sauce_label),
                        color = Color(0xFFB71C1C),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp
                    )
                    ChiliIcon(Modifier.size(28.dp))
                }
            }
        )

        // Foreground details
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TomatoSlice(
                Modifier
                    .size(85.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 55.dp, y = 15.dp)
            )
            
            ChiliIcon(
                Modifier
                    .size(65.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-35).dp, y = 20.dp)
            )

            GarlicIcon(
                Modifier
                    .size(45.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = 10.dp)
            )
        }
    }
}

@Composable
private fun SauceBottle(
    modifier: Modifier = Modifier,
    bottleColor: Color,
    capColor: Color,
    labelContent: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Bottle Body
            val bodyPath = Path().apply {
                moveTo(w * 0.2f, h * 0.95f)
                quadraticTo(w * 0.2f, h, w * 0.5f, h)
                quadraticTo(w * 0.8f, h, w * 0.8f, h * 0.95f)
                lineTo(w * 0.85f, h * 0.4f)
                quadraticTo(w * 0.85f, h * 0.25f, w * 0.5f, h * 0.25f)
                quadraticTo(w * 0.15f, h * 0.25f, w * 0.15f, h * 0.4f)
                close()
            }
            drawPath(bodyPath, bottleColor)
            drawPath(bodyPath, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 4f))

            // Cap
            val capPath = Path().apply {
                moveTo(w * 0.35f, h * 0.25f)
                lineTo(w * 0.3f, h * 0.15f)
                quadraticTo(w * 0.3f, h * 0.05f, w * 0.5f, h * 0.05f)
                quadraticTo(w * 0.7f, h * 0.05f, w * 0.7f, h * 0.15f)
                lineTo(w * 0.65f, h * 0.25f)
                close()
            }
            drawPath(capPath, capColor)
            drawPath(capPath, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 4f))

            // Highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(w * 0.22f, h * 0.4f),
                size = Size(w * 0.08f, h * 0.3f),
                cornerRadius = CornerRadius(20f, 20f)
            )
        }

        // Face
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.TopCenter)
                .offset(y = 45.dp)
        ) {
            IllustrationFace()
        }

        // Label
        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.BottomCenter)
                .offset(y = (-15).dp)
                .background(Color(0xFFFFF9E1), RoundedCornerShape(8.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            labelContent()
        }
    }
}

@Composable
private fun IllustrationFace(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IllustrationEye(Modifier.align(Alignment.CenterStart).size(18.dp))
            IllustrationEye(Modifier.align(Alignment.CenterEnd).size(18.dp))
        }
        
        Canvas(modifier = Modifier.size(24.dp, 12.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                quadraticTo(size.width / 2f, size.height * 1.5f, size.width, 0f)
            }
            drawPath(path, Color.Black, style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun IllustrationEye(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, CircleShape)
            .padding(1.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Black, radius = size.minDimension / 3f)
            drawCircle(Color.White, radius = size.minDimension / 10f, center = Offset(center.x + 2f, center.y - 2f))
        }
    }
}

@Composable
private fun TomatoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color(0xFFD32F2F), radius = size.minDimension * 0.4f)
        val stem = Path().apply {
            moveTo(center.x, center.y - size.height * 0.3f)
            lineTo(center.x - 5f, center.y - size.height * 0.5f)
            lineTo(center.x + 5f, center.y - size.height * 0.5f)
            close()
        }
        drawPath(stem, Color(0xFF388E3C))
    }
}

@Composable
private fun TomatoSlice(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color(0xFFD32F2F), radius = size.minDimension * 0.45f)
        drawCircle(Color(0xFFB71C1C), radius = size.minDimension * 0.4f, style = Stroke(width = 4f))
        for (i in 0 until 6) {
            val angle = i * 60f
            val x = center.x + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * size.width * 0.2f
            val y = center.y + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * size.height * 0.2f
            drawCircle(Color(0xFFFFD54F), radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun MustardSplat(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.5f)
            quadraticTo(size.width * 0.5f, size.height * 0.1f, size.width * 0.8f, size.height * 0.5f)
            quadraticTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.2f, size.height * 0.5f)
        }
        drawPath(path, Color(0xFFFFD54F))
    }
}

@Composable
private fun ChiliIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.2f)
            quadraticTo(size.width * 0.8f, size.height * 0.5f, size.width * 0.3f, size.height * 0.9f)
            quadraticTo(size.width * 0.1f, size.height * 0.5f, size.width * 0.2f, size.height * 0.2f)
        }
        drawPath(path, Color(0xFFD32F2F))
        val stem = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.2f)
            lineTo(size.width * 0.1f, size.height * 0.1f)
        }
        drawPath(stem, Color(0xFF388E3C), style = Stroke(width = 4f))
    }
}

@Composable
private fun GarlicIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawOval(Color(0xFFF5F5F5), topLeft = Offset(0f, size.height * 0.2f), size = Size(size.width, size.height * 0.8f))
        val tip = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width * 0.4f, size.height * 0.3f)
            lineTo(size.width * 0.6f, size.height * 0.3f)
            close()
        }
        drawPath(tip, Color(0xFFF5F5F5))
    }
}

@Preview(showBackground = true)
@Composable
private fun SaucesIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SaucesIllustration()
        }
    }
}
