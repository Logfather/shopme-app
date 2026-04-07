package de.shopme.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.presentation.state.ShoppingState
import de.shopme.ui.illustration.icons.bags.SadBagIllustration
import de.shopme.ui.illustration.icons.bags.HappyBagIllustration
import de.shopme.ui.theme.BrandGreen

@Composable
fun InviteScreen(
    state: ShoppingState,
    message: String? = null,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null
) {

    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "dialogScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val bounceOffset by animateDpAsState(
        targetValue = if (startAnimation) (-4).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonBounce"
    )

    val pulse = rememberInfiniteTransition(label = "buttonPulse")

    val glow by pulse.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnim"
    )

    val dialogGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.65f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .padding(24.dp)
                .scale(scale)
        ) {

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .background(dialogGradient)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        SadBagIllustration(
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        val senderName = state.inviteSenderName

                        Log.d("INVITE", "InviteScreen: senderName=$senderName")

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = senderName.takeUnless { it.isNullOrBlank() } ?: "Jemand",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "hat dich eingeladen",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Gemeinsamen Einkauf starten",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(
                            visible = startAnimation,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(420)
                            ) + fadeIn(tween(350))
                        ) {

                            val spotlight = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(spotlight)
                                        .pointerInput(Unit) {}
                                )

                                val floating = rememberInfiniteTransition(label = "bagFloat")

                                val floatAnim by floating.animateFloat(
                                    initialValue = -6f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "floatAnim"
                                )

                                HappyBagIllustration(
                                    modifier = Modifier
                                        .offset(y = floatAnim.dp)
                                        .fillMaxWidth(0.6f)
                                        .aspectRatio(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // 🔥 FIX: Loading State integriert
                        if (state.isJoining) {

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(vertical = 8.dp),
                                color = BrandGreen
                            )

                        } else if (onAccept != null && onDecline != null) {

                            Button(
                                onClick = {
                                    if (!state.isJoining) {
                                        onAccept?.invoke()
                                    }
                                },
                                enabled = !state.isJoining,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .offset(y = bounceOffset)
                                    .shadow(
                                        elevation = glow.coerceAtMost(12f).dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = BrandGreen,
                                        spotColor = BrandGreen
                                    ),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Jetzt öffnen")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onDecline,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Später")
                            }
                        }
                    }
                }
            }
        }
    }
}