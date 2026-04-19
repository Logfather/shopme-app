package de.shopme.ui.illustration.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import de.shopme.ui.theme.BrandWhite
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ShoppingBagAnimation(
    isVisible: Boolean,
    isDeleting: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {

        // 🛍️ Bag
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = BrandWhite,
            modifier = Modifier.size(64.dp)
        )

        // ✨ Animated Items
        repeat(6) { index ->
            AnimatedItem(
                index = index,
                isDeleting = isDeleting
            )
        }
    }
}

@Composable
private fun AnimatedItem(
    index: Int,
    isDeleting: Boolean
) {

    val startX = remember { Random.nextInt(-120, 120) }
    val startY = remember {
        if (isDeleting) Random.nextInt(-200, -80) // von oben
        else Random.nextInt(-120, 120)
    }

    val animX = remember { Animatable(startX.toFloat()) }
    val animY = remember { Animatable(startY.toFloat()) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index * 120L)

        alpha.animateTo(1f, tween(200))

        animX.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )

        animY.animateTo(
            targetValue = if (isDeleting) -150f else 0f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )

        alpha.animateTo(0f, tween(200))
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    animX.value.toInt(),
                    animY.value.toInt()
                )
            }
            .size(10.dp)
            .alpha(alpha.value)
            .background(BrandWhite, CircleShape)
    )
}