package de.shopme.ui.illustration.animations

import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode

@Composable
fun rememberFakeProgress(isActive: Boolean): Float {

    if (!isActive) return 1f

    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressAnim"
    )

    return progress
}