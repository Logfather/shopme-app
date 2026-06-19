package de.shopme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.shopme.presentation.nutrition.model.NutritionDetail

@Composable
fun NutritionValuesCard(

    detail: NutritionDetail

) {

    Card(

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(

            containerColor = Color.White.copy(alpha = 0.45f)

        )

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            NutritionValueRow(
                "Kalorien",
                "${detail.values.calories.toInt()} kcal"
            )

            NutritionValueRow(
                "Eiweiß",
                "${detail.values.protein} g"
            )

            NutritionValueRow(
                "Fett",
                "${detail.values.fat} g"
            )

            NutritionValueRow(
                "Gesättigte Fettsäuren",
                "${detail.values.saturatedFat} g"
            )

            NutritionValueRow(
                "Kohlenhydrate",
                "${detail.values.carbohydrates} g"
            )

            NutritionValueRow(
                "Zucker",
                "${detail.values.sugar} g"
            )

            NutritionValueRow(
                "Ballaststoffe",
                "${detail.values.fiber} g"
            )

            NutritionValueRow(
                "Salz",
                "${detail.values.salt} g"
            )

        }

    }

}