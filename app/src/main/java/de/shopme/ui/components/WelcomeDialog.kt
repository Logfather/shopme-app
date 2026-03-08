package de.shopme.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import de.shopme.R
import de.shopme.ui.illustration.HeroBagIllustration
import de.shopme.ui.illustration.ShoppingBagIllustration
import de.shopme.ui.theme.BrandGreen

@Composable
fun WelcomeDialog(
    onCreateFirstList: () -> Unit
) {

    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = 260,
            easing = FastOutSlowInEasing
        ),
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

    Box(
        modifier = Modifier.scale(scale)
    ) {
        val pulse = rememberInfiniteTransition(label = "buttonPulse")

        val glow by pulse.animateFloat(
            initialValue = 6f,
            targetValue = 14f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1400,
                    easing = FastOutSlowInEasing,
                    delayMillis = 400
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAnim"
        )

        val dialogGradient = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        )

        AlertDialog(
            onDismissRequest = { },

            shape = RoundedCornerShape(28.dp),

            containerColor = Color.Transparent,

            tonalElevation = 6.dp,

            confirmButton = {

                Button(
                    onClick = onCreateFirstList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        HeroBagIllustration(
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Erste Liste erstellen",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            },

            title = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {



                    HeroBagIllustration(
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
                }
            },

            text = {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {

                    Box(
                        modifier = Modifier
                            .background(dialogGradient)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Erstelle deine erste Einkaufsliste\nfür deinen Lieblingsmarkt.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Du kannst später jederzeit weitere Listen hinzufügen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            AnimatedVisibility(
                                visible = startAnimation,
                                enter =
                                    slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(420)
                                    ) + fadeIn(
                                        animationSpec = tween(350)
                                    )
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
                                    )
                                    val floating = rememberInfiniteTransition(label = "bagFloat")

                                    val floatAnim by floating.animateFloat(
                                        initialValue = -6f,
                                        targetValue = 6f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(
                                                durationMillis = 2600,
                                                easing = FastOutSlowInEasing
                                            ),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "floatAnim"
                                    )

                                    ShoppingBagIllustration(
                                        modifier = Modifier
                                            .offset(y = floatAnim.dp)
                                            .sizeIn(maxWidth = 220.dp)
                                            .fillMaxWidth(0.62f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        )
    }
}