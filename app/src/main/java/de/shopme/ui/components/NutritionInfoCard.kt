package de.shopme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.ui.theme.BrandBlack

@Composable
fun NutritionInfoCard(

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

            Text(

                text = detail.infoTitle,

                style = MaterialTheme.typography.titleMedium,

                color = BrandBlack

            )

            Spacer(

                Modifier.height(8.dp)

            )

            Text(

                text = detail.infoText,

                style = MaterialTheme.typography.bodyMedium,

                color = BrandBlack

            )
        }
    }
}