package de.shopme.platform.hivra.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HivraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    val shape = RoundedCornerShape(20.dp)

    val cardModifier = modifier
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)

    val clickableModifier = if (onClick != null) {
        cardModifier.clickable { onClick() }
    } else cardModifier

    Column(
        modifier = clickableModifier.padding(16.dp)
    ) {
        content()
    }
}