package de.shopme.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.shopme.presentation.components.welcome.OlafIllustration
import de.shopme.presentation.components.welcome.OlafSpeechBubble
import de.shopme.presentation.components.welcome.WelcomeActionButton
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandCreme
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(

    onCreateFirstList: () -> Unit

) {

    var showOlaf by remember {

        mutableStateOf(false)

    }

    var showSpeech by remember {

        mutableStateOf(false)

    }

    var showButton by remember {

        mutableStateOf(false)

    }

    var hasClicked by remember {

        mutableStateOf(false)

    }

    LaunchedEffect(Unit) {

        showOlaf = true

        delay(300)

        showSpeech = true

        delay(200)

        showButton = true

    }

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.5f))

    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.SpaceEvenly

        ) {

            AnimatedVisibility(

                visible = showOlaf

            ) {

                OlafIllustration()

            }

            AnimatedVisibility(

                visible = showSpeech

            ) {

                OlafSpeechBubble(

                    visible = true

                )

            }

            AnimatedVisibility(

                visible = showButton

            ) {

                WelcomeActionButton(

                    enabled = !hasClicked,

                    onClick = {

                        if (!hasClicked) {

                            hasClicked = true

                            onCreateFirstList()

                        }

                    }

                )

            }

            Column(

                modifier = Modifier
                    .background(
                        color = BrandCreme,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Text(

                    text = "Hivra",

                    style = MaterialTheme.typography.bodySmall,

                    color = BrandBlack.copy(alpha = 0.65f)

                )

                Text(

                    text = "Made with ☕ in Bavaria",

                    style = MaterialTheme.typography.bodySmall,

                    color = BrandBlack.copy(alpha = 0.45f)

                )

            }

        }

    }

}