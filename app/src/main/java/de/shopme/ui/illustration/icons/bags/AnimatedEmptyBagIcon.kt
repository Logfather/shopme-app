package de.shopme.ui.illustration.icons.bags

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedEmptyBagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyBag")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    EmptyBagIcon(
        modifier = modifier.graphicsLayer {
            translationY = floatY
            this.alpha = alpha
        },
        size = size
    )
}