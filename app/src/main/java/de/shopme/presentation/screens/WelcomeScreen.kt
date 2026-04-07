package de.shopme.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.ui.illustration.icons.bags.SadBagIllustration
import de.shopme.ui.illustration.icons.bags.ShoppingBagIllustration
import de.shopme.ui.theme.BrandGreen

@Composable
fun WelcomeScreen(
    onCreateFirstList: () -> Unit
) {

    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val bounceOffset by animateDpAsState(
        targetValue = if (startAnimation) (-4).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    val pulse = rememberInfiniteTransition(label = "pulse")

    val glow by pulse.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 🔥 FIX: Dialog bleibt hell (unabhängig vom Theme)
    val dialogGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            Color(0xFFF5F5F5).copy(alpha = 0.85f)
        )
    )

    // 🔥 FULLSCREEN OVERLAY (gleich wie InviteScreen)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)), // 👈 WICHTIG
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

                        // HEADER
                        SadBagIllustration(
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Willkommen bei ShopMe",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Deine Einkaufsliste für jeden Markt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // CONTENT
                        Text(
                            text = "Erstelle deine erste Einkaufsliste\nfür deinen Lieblingsmarkt.",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Du kannst später jederzeit weitere Listen hinzufügen.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AnimatedVisibility(
                            visible = startAnimation,
                            enter =
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(420)
                                ) + fadeIn(tween(350))
                        ) {

                            val spotlight = Brush.radialGradient(
                                colors = listOf(
                                    BrandGreen.copy(alpha = 0.15f),
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
                                )

                                val floating = rememberInfiniteTransition(label = "float")

                                val floatAnim by floating.animateFloat(
                                    initialValue = -6f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2600),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "floatAnim"
                                )

                                ShoppingBagIllustration(
                                    modifier = Modifier
                                        .offset(y = floatAnim.dp)
                                        .fillMaxWidth(0.6f)
                                        .aspectRatio(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BUTTON
                        Button(
                            onClick = onCreateFirstList,
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

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                SadBagIllustration(
                                    modifier = Modifier.size(22.dp)
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    text = "Erste Liste erstellen",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}