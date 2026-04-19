package de.shopme.ui.illustration.icons.indicators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

@Composable
fun CrumbleOverlay(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "crumbs")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "crumbMove"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val crumbCount = 5

        repeat(crumbCount) {
            val x = size.width * progress - offsetX + it * 20
            val y = size.height / 2 + (it * 3)

            drawCircle(
                color = Color(0xFFFFC107),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}