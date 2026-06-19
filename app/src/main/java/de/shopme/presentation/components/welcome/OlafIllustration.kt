package de.shopme.presentation.components.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun OlafIllustration(

    modifier: Modifier = Modifier

) {

    val transition = rememberInfiniteTransition(

        label = "olaf"

    )

    val floatOffset by transition.animateFloat(

        initialValue = -2f,

        targetValue = 2f,

        animationSpec = infiniteRepeatable(

            animation = tween(

                durationMillis = 3200,

                easing = FastOutSlowInEasing

            ),

            repeatMode = RepeatMode.Reverse

        ),

        label = "float"

    )

    val coffeeRotation by transition.animateFloat(

        initialValue = -2f,

        targetValue = 2f,

        animationSpec = infiniteRepeatable(

            animation = tween(

                durationMillis = 5000,

                easing = FastOutSlowInEasing

            ),

            repeatMode = RepeatMode.Reverse

        ),

        label = "coffee"

    )

    Image(

        painter = painterResource(

            id = R.drawable.olaf_transparent

        ),

        contentDescription = "Olaf",

        modifier = modifier
            .fillMaxWidth(0.55f)
            .aspectRatio(1f)
            .offset(

                y = floatOffset.dp

            )
            .graphicsLayer {

                rotationZ = coffeeRotation

            }

    )

}