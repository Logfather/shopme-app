package de.shopme.ui.illustration.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.ui.illustration.icons.indicators.CrumbleOverlay

@Composable
fun BagProgressBar(
    progress: Float, // 0f - 1f
    modifier: Modifier = Modifier,
    height: Dp = 24.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "bagProgress"
    )

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8D5B7))
    ) {

        // 📦 „Füllung“
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFB74D))
        )

        // ✨ Krümel Overlay
        if (animatedProgress > 0.05f) {
            CrumbleOverlay(progress = animatedProgress)
        }
    }
}