package de.shopme.presentation.components.foodintelligence

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandWhite

@Composable
fun InfoChip(

    onClick: () -> Unit

) {

    TextButton(

        onClick = onClick,

        shape = RoundedCornerShape(12.dp),

        colors = ButtonDefaults.textButtonColors(

            containerColor = BrandGreen

        ),

        elevation = ButtonDefaults.buttonElevation(

            defaultElevation = 2.dp

        )

    ) {

        Text(

            text = "Info",

            style = MaterialTheme.typography.bodyMedium,

            color = BrandWhite

        )

    }

}