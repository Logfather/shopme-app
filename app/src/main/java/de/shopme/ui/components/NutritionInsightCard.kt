package de.shopme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.domain.nutrition.model.NutritionInsight
import de.shopme.ui.theme.BrandGreen

@Composable
fun NutritionInsightCard(

    insight: NutritionInsight

) {

    Column(

        modifier = Modifier.fillMaxWidth()

    ) {

        Text(

            text = insight.title,

            style =
                MaterialTheme.typography.titleMedium,

            color = BrandGreen

        )

        Spacer(

            modifier =
                Modifier.height(8.dp)

        )

        Text(

            text = insight.text,

            style =
                MaterialTheme.typography.bodyMedium

        )
    }
}