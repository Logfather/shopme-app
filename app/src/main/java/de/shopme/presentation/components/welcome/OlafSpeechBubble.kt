package de.shopme.presentation.components.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandCreme

@Composable
fun OlafSpeechBubble(

    visible: Boolean,

    modifier: Modifier = Modifier

) {

    AnimatedVisibility(

        visible = visible,

        enter =
            slideInHorizontally(

                initialOffsetX = { it / 2 },

                animationSpec = tween(450)

            ) + fadeIn(

                animationSpec = tween(450)

            )

    ) {

        Card(

            modifier = modifier,

            shape = RoundedCornerShape(24.dp),

            colors = CardDefaults.cardColors(

                containerColor = BrandCreme,

                contentColor = BrandBlack

            )

        ) {

            Column(

                modifier = Modifier.padding(20.dp)

            ) {

                Text(

                    text = "Dich hab ich jetzt wirklich nicht erwartet!",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold

                )

                Text(

                    text =
                        """

Hi, ich bin Olaf.

Ich habe diese App gebaut, weil mein Gedächtnis zwar für Softwarearchitekturen reicht, aber erstaunlich selten für Butter.

Daher vertraue ich lieber einer Einkaufsliste als meinem Erinnerungsvermögen.

Falls hier alles funktioniert, war das selbstredend Absicht.

Und wenn nicht, sag's mir einfach.

Ich repariere Dinge ganz gern.

Viel Spaß!

                    – Olaf ☕

                    """.trimIndent(),

                    style = MaterialTheme.typography.bodyMedium

                )

            }

        }

    }

}