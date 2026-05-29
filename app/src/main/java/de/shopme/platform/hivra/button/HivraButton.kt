package de.shopme.platform.hivra.button

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun HivraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: HivraButtonType = HivraButtonType.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {

    val colors = when (type) {

        HivraButtonType.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )

        HivraButtonType.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
        )

        HivraButtonType.Danger -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White
        )

        HivraButtonType.Text -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    }

    val shape = RoundedCornerShape(16.dp)

    if (type == HivraButtonType.Text) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            modifier = modifier
        ) {
            ButtonContent(text, icon)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = shape,
            modifier = modifier
        ) {
            ButtonContent(text, icon)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }

        Text(text)
    }
}