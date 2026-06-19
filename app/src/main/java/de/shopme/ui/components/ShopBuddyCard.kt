package de.shopme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.shopme.presentation.components.ShopBuddy
import de.shopme.presentation.components.ShopBuddyState
import de.shopme.ui.theme.BrandBlack

@Composable
fun ShopBuddyCard(
    state: ShopBuddyState
) {

    Card(

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(

            containerColor =
                Color.White.copy(alpha = 0.45f)

        )

    ) {

        Row(

            modifier = Modifier.padding(16.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            ShopBuddy(

                mood = state.mood

            )

            Spacer(

                Modifier.width(12.dp)

            )

            Column {

                Text(

                    text = "ShopBuddy",

                    style = MaterialTheme.typography.titleMedium,

                    color = BrandBlack

                )

                Spacer(

                    Modifier.width(4.dp)

                )

                Text(

                    text = state.text,

                    style = MaterialTheme.typography.bodyMedium,

                    color = BrandBlack

                )
            }
        }
    }
}