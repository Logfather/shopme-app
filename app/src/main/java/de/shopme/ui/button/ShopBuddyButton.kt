package de.shopme.ui.components.button

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ShopBuddyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ShopBuddyButtonType = ShopBuddyButtonType.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {

    val colors = when (type) {

        ShopBuddyButtonType.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
        )

        ShopBuddyButtonType.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f)
        )

        ShopBuddyButtonType.Danger -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.4f)
        )

        ShopBuddyButtonType.Text -> ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    }

    val elevation = if (type != ShopBuddyButtonType.Text) {
        ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    } else null

    val shape = MaterialTheme.shapes.medium

    when (type) {

        ShopBuddyButtonType.Text -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                colors = colors
            ) {
                ButtonContent(text, icon)
            }
        }

        else -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                shape = shape,
                colors = colors,
                elevation = elevation
            ) {
                ButtonContent(text, icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class ShopBuddyButtonType {
    Primary,
    Secondary,
    Text,
    Danger
}