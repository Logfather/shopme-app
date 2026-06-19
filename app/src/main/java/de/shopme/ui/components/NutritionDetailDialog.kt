package de.shopme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.domain.nutrition.model.NutritionInsight
import de.shopme.domain.nutrition.pipeline.ProductionNutritionPipeline
import de.shopme.domain.nutrition.service.NutritionInsightService
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandCreme
import de.shopme.ui.theme.BrandGreen

@Composable
fun NutritionDetailDialog(

    productName: String,

    nutritionReference: String,

    productionNutritionPipeline: ProductionNutritionPipeline,

    nutritionInsightService: NutritionInsightService,

    onDismiss: () -> Unit

) {

    var insight by remember {

        mutableStateOf<NutritionInsight?>(null)

    }

    var detail by remember {

        mutableStateOf<NutritionDetail?>(null)

    }

    LaunchedEffect(

        nutritionReference

    ) {

        detail =

            productionNutritionPipeline
                .getNutritionDetail(
                    nutritionReference
                )

        detail?.let {

            insight =

                nutritionInsightService
                    .getInsight(it)

        }
    }

    if (detail == null) {

        AlertDialog(

            onDismissRequest = onDismiss,

            containerColor = BrandCreme,

            title = {

                Text(

                    text = productName,

                    style =
                        MaterialTheme.typography.headlineSmall,

                    color = BrandBlack

                )

            },

            text = {

                Text(

                    text = "Nährwertinformationen werden geladen...",

                    color = BrandBlack

                )

            },

            confirmButton = {

                TextButton(

                    onClick = onDismiss

                ) {

                    Text(

                        "Schließen",

                        color = BrandGreen

                    )

                }
            }
        )

        return
    }

    val nutrition = detail!!

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = BrandCreme,

        title = {

            Column(

                modifier = Modifier.fillMaxWidth(),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Text(

                    text = productName,

                    style = MaterialTheme.typography.headlineSmall,

                    color = BrandBlack

                )

            }

        },

        text = {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Text(

                    text = "NutriScore",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = BrandBlack

                )

                Spacer(

                    modifier = Modifier.height(12.dp)

                )

                NutriScoreBadge(

                    score = nutrition.nutriScore

                )

                Spacer(

                    modifier = Modifier.height(32.dp)

                )

                NutritionValuesCard(

                    detail = nutrition

                )

                Spacer(

                    modifier = Modifier.height(20.dp)

                )

                insight?.let {

                    NutritionInsightCard(

                        insight = it

                    )

                }

                Spacer(

                    modifier = Modifier.height(20.dp)

                )

                ShopBuddyCard(

                    state = nutrition.buddyState

                )

            }

        },

        confirmButton = {

            TextButton(

                onClick = onDismiss

            ) {

                Text(

                    text = "Schließen",

                    color = BrandGreen,

                    style = MaterialTheme.typography.bodyLarge

                )

            }

        }

    )
}