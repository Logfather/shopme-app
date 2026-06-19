package de.shopme.presentation.components.foodintelligence

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandCreme

@Composable
fun FoodIntelligenceDetail(

    result: FoodDimensionResult,

    onDismiss: () -> Unit

) {

    val dimension = result.dimension

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = BrandCreme,

        title = {

            Text(

                text = dimension.title

            )

        },

        text = {

            Column {

                Text(

                    text = result.dimension.description,

                    style = MaterialTheme.typography.bodyMedium

                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                Text(

                    text = "Bewertung",

                    style = MaterialTheme.typography.titleSmall

                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(

                    text = "${result.trafficLight.icon} ${result.summary}",

                    style = MaterialTheme.typography.bodyMedium

                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(

                    text = result.recommendation,

                    style = MaterialTheme.typography.bodyMedium

                )

            }

        },

        confirmButton = {

            TextButton(

                onClick = onDismiss

            ) {

                Text("Schließen")

            }

        }

    )

}