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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandWhite
@Composable
fun DairyIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.dairy_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        BackgroundSplashes(modifier = Modifier.fillMaxSize())
        MilkBottleCharacter(
            modifier = Modifier
                .fillMaxSize(0.42f)
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 35.dp)
        )
        MilkCartonCharacter(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .align(Alignment.TopCenter)
                .offset(x = 45.dp, y = 15.dp)
        )
        CheeseWedgeCharacter(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 30.dp)
        )
        YogurtCupCharacter(
            modifier = Modifier
                .fillMaxSize(0.38f)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = 60.dp)
        )
        CheeseWheelCharacter(
            modifier = Modifier
                .fillMaxSize(0.42f)
                .align(Alignment.BottomCenter)
                .offset(x = 20.dp, y = (-10).dp)
        )
        EggsCharacter(
            modifier = Modifier
                .fillMaxSize(0.32f)
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-15).dp)
        )
        ButterCharacter(
            modifier = Modifier
                .fillMaxSize(0.32f)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-35).dp)
        )
    }
}
@Composable
private fun BackgroundSplashes(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawSplashDroplet(Offset(size.width * 0.12f, size.height * 0.22f), 12f, -35f)
        drawSplashDroplet(Offset(size.width * 0.08f, size.height * 0.28f), 10f, -50f)
        drawSplashDroplet(Offset(size.width * 0.16f, size.height * 0.3f), 8f, -20f)
        drawSplashDroplet(Offset(size.width * 0.78f, size.height * 0.18f), 15f, 25f)
        drawSplashDroplet(Offset(size.width * 0.88f, size.height * 0.22f), 12f, 45f)
        drawSplashDroplet(Offset(size.width * 0.75f, size.height * 0.28f), 10f, 15f)
        drawSplashDroplet(Offset(size.width * 0.85f, size.height * 0.32f), 8f, 40f)
    }
}
private fun DrawScope.drawSplashDroplet(center: Offset, radius: Float, rotation: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius * 1.5f)
        quadraticTo(center.x + radius, center.y, center.x, center.y + radius)
        quadraticTo(center.x - radius, center.y, center.x, center.y - radius * 1.5f)
        close()
    }
    rotate(rotation, center) {
        drawPath(path, BrandWhite)
        drawPath(path, Color(0xFFE0E0E0), style = Stroke(width = 1.5f))
    }
}
@Composable
private fun MilkBottleCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val bottlePath = Path().apply {
                moveTo(w * 0.35f, h * 0.2f)
                quadraticTo(w * 0.35f, h * 0.15f, w * 0.5f, h * 0.15f)
                quadraticTo(w * 0.65f, h * 0.15f, w * 0.65f, h * 0.2f)
                lineTo(w * 0.65f, h * 0.3f)
                quadraticTo(w * 0.65f, h * 0.45f, w * 0.85f, h * 0.5f)
                lineTo(w * 0.85f, h * 0.9f)
                quadraticTo(w * 0.85f, h * 0.98f, w * 0.5f, h * 0.98f)
                quadraticTo(w * 0.15f, h * 0.98f, w * 0.15f, h * 0.9f)
                lineTo(w * 0.15f, h * 0.5f)
                quadraticTo(w * 0.35f, h * 0.45f, w * 0.35f, h * 0.3f)
                close()
            }
            drawPath(bottlePath, BrandWhite)
            drawPath(bottlePath, BrandBlack, style = Stroke(width = 4f))
            val capPath = Path().apply {
                moveTo(w * 0.32f, h * 0.12f)
                lineTo(w * 0.68f, h * 0.12f)
                lineTo(w * 0.65f, h * 0.22f)
                lineTo(w * 0.35f, h * 0.22f)
                close()
            }
            drawPath(capPath, Color(0xFF1E88E5))
            drawPath(capPath, BrandBlack, style = Stroke(width = 3f))
        }
        CharacterFace(
            modifier = Modifier.offset(y = 15.dp),
            eyeSize = 18.dp,
            mouthSize = 22.dp
        )
    }
}
@Composable
private fun MilkCartonCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cartonPath = Path().apply {
                moveTo(w * 0.2f, h * 0.25f)
                lineTo(w * 0.8f, h * 0.2f)
                lineTo(w * 0.85f, h * 0.95f)
                lineTo(w * 0.15f, h * 0.98f)
                close()
            }
            drawPath(cartonPath, BrandWhite)
            drawPath(cartonPath, BrandBlack, style = Stroke(width = 4f))
            val topPath = Path().apply {
                moveTo(w * 0.2f, h * 0.25f)
                lineTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.8f, h * 0.2f)
                lineTo(w * 0.82f, h * 0.45f)
                lineTo(w * 0.18f, h * 0.5f)
                close()
            }
            drawPath(topPath, Color(0xFF1976D2))
            drawPath(topPath, BrandBlack, style = Stroke(width = 4f))
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = stringResource(R.string.milch_label),
                color = BrandWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(15.dp))
            CharacterFace(
                eyeSize = 20.dp,
                mouthSize = 26.dp,
                isWinking = true
            )
        }
    }
}
@Composable
private fun CheeseWedgeCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val wedgePath = Path().apply {
                moveTo(w * 0.1f, h * 0.4f)
                lineTo(w * 0.95f, h * 0.3f)
                lineTo(w * 0.92f, h * 0.85f)
                lineTo(w * 0.2f, h * 0.95f)
                close()
            }
            drawPath(wedgePath, Color(0xFFFFD54F))
            drawPath(wedgePath, BrandBlack, style = Stroke(width = 4f))
            drawCircle(Color(0xFFFBC02D), radius = 14f, center = Offset(w * 0.25f, h * 0.55f))
            drawCircle(Color(0xFFFBC02D), radius = 20f, center = Offset(w * 0.78f, h * 0.45f))
            drawCircle(Color(0xFFFBC02D), radius = 12f, center = Offset(w * 0.52f, h * 0.78f))
            drawCircle(Color(0xFFFBC02D), radius = 16f, center = Offset(w * 0.88f, h * 0.68f))
            drawCircle(Color(0xFFFBC02D), radius = 10f, center = Offset(w * 0.32f, h * 0.88f))
        }
        CharacterFace(
            modifier = Modifier.offset(y = 12.dp),
            eyeSize = 20.dp,
            mouthSize = 28.dp
        )
    }
}
@Composable
private fun YogurtCupCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cupPath = Path().apply {
                moveTo(w * 0.15f, h * 0.5f)
                lineTo(w * 0.85f, h * 0.5f)
                lineTo(w * 0.75f, h * 0.95f)
                lineTo(w * 0.25f, h * 0.95f)
                close()
            }
            drawPath(cupPath, Color(0xFF2196F3))
            drawPath(cupPath, BrandBlack, style = Stroke(width = 4f))
            drawRoundRect(
                color = BrandWhite,
                topLeft = Offset(w * 0.1f, h * 0.45f),
                size = Size(w * 0.8f, h * 0.1f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.1f, h * 0.45f),
                size = Size(w * 0.8f, h * 0.1f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 3f)
            )

            val dollopPath = Path().apply {
                moveTo(w * 0.3f, h * 0.45f)
                quadraticTo(w * 0.3f, h * 0.1f, w * 0.55f, h * 0.1f)
                quadraticTo(w * 0.8f, h * 0.1f, w * 0.8f, h * 0.45f)
            }
            drawPath(dollopPath, BrandWhite)
            drawPath(dollopPath, BrandBlack, style = Stroke(width = 3f))
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = stringResource(R.string.joghurt_label),
                color = Color.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}
@Composable
private fun CheeseWheelCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val wheelPath = Path().apply {
                moveTo(w * 0.1f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.4f, w * 0.9f, h * 0.5f)
                lineTo(w * 0.9f, h * 0.9f)
                quadraticTo(w * 0.5f, h * 1.0f, w * 0.1f, h * 0.9f)
                close()
            }
            drawPath(wheelPath, Color(0xFFE64A19))
            drawPath(wheelPath, BrandBlack, style = Stroke(width = 4f))

            drawCircle(Color(0xFFFBC02D).copy(alpha = 0.5f), radius = 10f, center = Offset(w * 0.75f, h * 0.8f))
        }
        CharacterFace(
            modifier = Modifier.offset(y = 12.dp),
            eyeSize = 18.dp,
            mouthSize = 24.dp
        )
    }
}
@Composable
private fun EggsCharacter(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
        Egg(modifier = Modifier.size(40.dp).offset(y = 22.dp))
        Egg(modifier = Modifier.size(46.dp).offset(y = 10.dp))
        Egg(modifier = Modifier.size(40.dp).offset(y = 22.dp))
    }
}
@Composable
private fun Egg(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawOval(Color(0xFFFFFDE7), size = size)
        drawOval(BrandBlack, size = size, style = Stroke(width = 3f))
    }
}
@Composable
private fun ButterCharacter(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawOval(
                color = Color(0xFF795548),
                topLeft = Offset(w * 0.1f, h * 0.75f),
                size = Size(w * 0.9f, h * 0.25f)
            )
            drawOval(
                color = BrandBlack,
                topLeft = Offset(w * 0.1f, h * 0.75f),
                size = Size(w * 0.9f, h * 0.25f),
                style = Stroke(width = 2f)
            )
            drawRoundRect(
                color = Color(0xFFFFF176),
                topLeft = Offset(w * 0.25f, h * 0.55f),
                size = Size(w * 0.7f, h * 0.4f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = BrandBlack,
                topLeft = Offset(w * 0.25f, h * 0.55f),
                size = Size(w * 0.7f, h * 0.4f),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 3f)
            )
            val curlPath = Path().apply {
                moveTo(w * 0.4f, h * 0.55f)
                quadraticTo(w * 0.65f, h * 0.15f, w * 0.9f, h * 0.55f)
            }
            drawPath(curlPath, Color(0xFFFFEE58))
            drawPath(curlPath, BrandBlack, style = Stroke(width = 3f))
        }
    }
}
@Composable
private fun CharacterFace(
    modifier: Modifier = Modifier,
    eyeSize: Dp = 16.dp,
    mouthSize: Dp = 20.dp,
    isWinking: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CharacterEye(eyeSize = eyeSize)
            if (isWinking) {
                WinkEye(eyeSize = eyeSize)
            } else {
                CharacterEye(eyeSize = eyeSize)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Mouth(modifier = Modifier.size(mouthSize))
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
            drawCircle(BrandBlack, radius = size.minDimension * 0.38f, center = center)
            drawCircle(BrandWhite, radius = size.minDimension * 0.15f, center = Offset(center.x + size.width * 0.18f, center.y - size.height * 0.18f))
        }
    }
}
@Composable
private fun WinkEye(eyeSize: Dp) {
    Canvas(modifier = Modifier.size(eyeSize)) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.55f)
            quadraticTo(size.width * 0.5f, size.height * 0.15f, size.width * 0.85f, size.height * 0.55f)
        }
        drawPath(path, BrandBlack, style = Stroke(width = 5f))
    }
}
@Composable
private fun Mouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.3f)
            quadraticTo(size.width / 2, size.height * 1.4f, size.width, size.height * 0.3f)
            close()
        }
        drawPath(path, Color(0xFF8B0000))
        drawPath(path, BrandBlack, style = Stroke(width = 3f))
    }
}
@Preview(showBackground = true)
@Composable
private fun DairyIllustrationPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .background(BrandWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        DairyIllustration()
    }
}
