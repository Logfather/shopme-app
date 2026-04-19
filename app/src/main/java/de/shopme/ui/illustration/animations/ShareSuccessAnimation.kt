package de.shopme.ui.illustration.animations

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

import de.shopme.core.sound.SoundPlayer
import de.shopme.ui.theme.BrandWhite

@Composable
fun ShareSuccessAnimation(
    visible: Boolean,
    onFinished: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current

    val haptic = LocalHapticFeedback.current

    var impactTriggered by remember { mutableStateOf(false) }

    // 🔥 interner Start-Trigger
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d("SHARE_FLOW", "Animation START")

        // 🔥 kleiner Delay → garantiert Start-Frame sichtbar
        delay(50)

        startAnim = true
    }

    val transition = updateTransition(
        targetState = startAnim,
        label = "share_anim"
    )

    // 🔥 Flug X (rechts → mitte)
    val offsetX by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 700, easing = FastOutSlowInEasing)
        },
        label = "offsetX"
    ) { state ->
        if (state) 0f else 800f
    }

    // 🔥 echte Kurve (Parabel)
    val offsetY = -0.0012f * (offsetX - 400f) * (offsetX - 400f) + 150f

    // 🔥 Rotation
    val rotation by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 700)
        },
        label = "rotation"
    ) { state ->
        if (state) 0f else 25f
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (impactTriggered) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 300
            0f at 0
            -10f at 50
            8f at 100
            -6f at 150
            3f at 200
            0f at 300
        },
        label = "shake"
    )

    // 🔥 Bounce (nach Landung)
    val scale by animateFloatAsState(
        targetValue = when {
            !startAnim -> 0.8f
            impactTriggered -> 1.25f // 🔥 Overshoot beim Impact
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // 🔥 Auto-Finish
    LaunchedEffect(startAnim) {

        if (!startAnim) return@LaunchedEffect

        val impactDelay = (700 * 0.9f).toLong()

        delay(impactDelay)

// 🔥 FRAME-GENAU
        withFrameNanos {

            impactTriggered = true

            // 🔊 Sound
            SoundPlayer.play()

            // 📳 HAPTIC
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        delay(3550) // restliche Zeit (gesamt ~2200ms)

        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset {
                IntOffset(
                    x = (offsetX + shakeOffset).roundToInt(),
                    y = offsetY.roundToInt()
                )
            }
        ) {

            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = BrandWhite,
                modifier = Modifier
                    .size(72.dp)
                    .rotate(rotation)
            )

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(500))
            ) {
                Text(
                    text = "Einladung gesendet",
                    color = BrandWhite
                )
            }
        }

        ParticleBurst(trigger = impactTriggered)
    }
}