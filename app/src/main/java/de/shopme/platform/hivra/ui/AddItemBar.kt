package de.shopme.platform.hivra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.platform.hivra.button.HivraButton
import de.shopme.platform.hivra.input.HivraTextField


@Composable
fun AddItemBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        HivraTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Was fehlt?",
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        HivraButton(
            text = "Add",
            onClick = onAdd
        )
    }
}