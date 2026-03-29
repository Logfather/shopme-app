package de.shopme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import de.shopme.ui.illustration.buttons.CartoonMicrophone

@Composable
fun RecordingButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeColor = MaterialTheme.colorScheme.error
    val inactiveColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {

        // Pulsierender Ring
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(
                        activeColor.copy(alpha = 0.35f),
                        CircleShape
                    )
            )
        }

        // Hauptbutton
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(75.dp)
                .background(
                    if (isRecording) activeColor else inactiveColor,
                    CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClick()
                }
        ) {

            CartoonMicrophone(
                modifier = Modifier.size(48.dp)
            )
        }
    }
}