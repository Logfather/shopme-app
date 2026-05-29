package de.shopme.platform.nimblu.state

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import de.shopme.platform.hivra.button.HivraButton

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    icon: Painter,
    action: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        Spacer(Modifier.height(64.dp))

        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(title, style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(8.dp))

        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(24.dp))

        HivraButton(
            text = "Starten",
            onClick = action
        )
    }
}