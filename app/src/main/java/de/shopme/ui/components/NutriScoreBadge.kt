package de.shopme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.NutriScoreColors

private val BadgeWidth = 38.dp
private val BadgeCornerRadius = 10.dp

@Composable
fun NutriScoreBadge(
    score: String,
    onClick: (() -> Unit)? = null
) {

    val backgroundColor =
        NutriScoreColors.fromScore(score)

    Box(
        modifier = Modifier
            .width(BadgeWidth)
            .clickable {

                onClick?.invoke()

            }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(
                    BadgeCornerRadius
                )
            )
            .padding(vertical = 8.dp),

        contentAlignment = Alignment.Center
    ) {

        Text(
            text = score.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}