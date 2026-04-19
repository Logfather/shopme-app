package de.shopme.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.shopme.ui.illustration.icons.indicators.CartoonProgressIndicator

@Composable
fun CartoonLoader(
    modifier: Modifier = Modifier,
    progress: Float? = null // 👈 NULL = indeterminate
) {

    val infiniteTransition = rememberInfiniteTransition()

    val animatedProgress = progress ?: infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200)
        ),
        label = "indeterminateProgress"
    ).value

    CartoonProgressIndicator(
        progress = animatedProgress,
        modifier = modifier
    )
}