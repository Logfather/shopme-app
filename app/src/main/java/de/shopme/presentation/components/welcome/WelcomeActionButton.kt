package de.shopme.presentation.components.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandWhite

@Composable
fun WelcomeActionButton(

    enabled: Boolean,

    onClick: () -> Unit

) {

    val transition = rememberInfiniteTransition(

        label = "welcomeButton"

    )

    val scale by transition.animateFloat(

        initialValue = 1f,

        targetValue = 1.02f,

        animationSpec = infiniteRepeatable(

            animation = tween(

                durationMillis = 1800,

                easing = FastOutSlowInEasing

            ),

            repeatMode = RepeatMode.Reverse

        ),

        label = "scale"

    )

    Button(

        onClick = onClick,

        enabled = enabled,

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),

        shape = RoundedCornerShape(18.dp),

        colors = ButtonDefaults.buttonColors(

            containerColor = BrandGreen,

            contentColor = BrandWhite

        ),

        elevation = ButtonDefaults.buttonElevation(

            defaultElevation = 4.dp,

            pressedElevation = 2.dp

        )

    ) {

        Text(

            text = "Erste Einkaufsliste erstellen",

            style = MaterialTheme.typography.labelLarge

        )

    }

}