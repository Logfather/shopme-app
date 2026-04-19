package de.shopme.ui.illustration.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandWhite
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


@Composable
fun ParticleBurst(trigger: Boolean) {

    if (!trigger) return

    val particles = remember(trigger) {
        List(18) {
            Particle(
                angle = (0..360).random().toFloat(),
                speed = (5..14).random().toFloat(),
                size = (4..9).random().toFloat(),
                color = randomBrandColor()
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        particles.forEach { particle ->
            ParticleItem(particle = particle, trigger = trigger)
        }
    }
}

@Composable
fun ParticleItem(
    particle: Particle,
    trigger: Boolean
) {

    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    }

    val p = progress.value

    // 🔥 Geschwindigkeit fällt ab (Reibung)
    val velocity = particle.speed * (1f - p * 0.4f)

    val distance = velocity * p * 60f

    val baseX = cos(Math.toRadians(particle.angle.toDouble()))
        .toFloat() * distance

    val baseY = sin(Math.toRadians(particle.angle.toDouble()))
        .toFloat() * distance

    // 🌍 echte Gravity (quadratisch)
    val gravity = p * p * 120f

    val x = baseX
    val y = baseY + gravity

    // 🌫 Motion Blur abhängig von Geschwindigkeit
    val blurAmount = (velocity * (1f - p)) * 0.8f

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .size(particle.size.dp)
            .blur(blurAmount.dp)
            .background(
                particle.color.copy(alpha = 1f - p)
            )
    )
}

data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

// 🌈 Brand Color System
fun randomBrandColor(): Color {
    return when (Random.nextInt(3)) {
        0 -> BrandWhite
        1 -> Color(0xFF4CAF50) // BrandGreen
        else -> Color(0xFF81C784) // helleres Grün
    }
}