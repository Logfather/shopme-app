package de.shopme.presentation.components.foodintelligence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.ui.icons.FoodIndicators
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandCreme

@Composable
fun FoodIntelligenceOverview(

    itemName: String,

    onDismiss: () -> Unit

) {

    var selectedSection by remember {

        mutableStateOf<FoodIntelligenceSection?>(null)

    }

    Card(

        colors = CardDefaults.cardColors(

            containerColor = BrandCreme,

            contentColor = BrandBlack

        )

    )  {

        Column(

            modifier = Modifier.padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            Text(

                text = "Detailinformationen zu $itemName"

            )

            FoodDimensionProvider.provide().forEach { dimension ->

                FoodIntelligenceRow(

                    indicator = FoodIndicators.GREEN,

                    title = dimension.title

                ) {

                    selectedSection = dimension.section

                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(

                    onClick = onDismiss

                ) {

                    Text("Schließen")

                }

            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(

                    text = "* Für weitere Informationen einen Punkt auswählen.",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )

            }

        }

    }

    selectedSection?.let { section ->

        FoodIntelligenceDetail(

            result = FoodDimensionResultProvider.find(

                section

            ),

            onDismiss = {

                selectedSection = null

            }

        )

    }


}

@Composable
private fun FoodIntelligenceRow(

    indicator: String,

    title: String,

    onClick: () -> Unit

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),

        verticalAlignment = Alignment.CenterVertically

    ) {

        Text(indicator)

        Spacer(

            modifier = Modifier.width(12.dp)

        )

        Text(

            text = title,

            modifier = Modifier.weight(1f)

        )

    }

}