package de.shopme.ui.app.topbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HivraMenu(

    expanded: Boolean,

    onDismiss: () -> Unit,

    onProfile: () -> Unit,

    onFoodIntelligence: () -> Unit,

    onBuildReport: () -> Unit

) {

    DropdownMenu(

        expanded = expanded,

        onDismissRequest = onDismiss

    ) {

        DropdownMenuItem(

            text = {

                Text("👤 Profil")

            },

            onClick = {

                onDismiss()

                onProfile()

            }

        )

        HorizontalDivider()

        Text(
            text = "Entwickler",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )

        DropdownMenuItem(

            text = {

                Text("🧠 Lebensmittelwissen")

            },

            onClick = {

                onDismiss()

                onFoodIntelligence()

            }

        )

        DropdownMenuItem(

            text = {

                Text("📊 Build Report")

            },

            onClick = {

                onDismiss()

                onBuildReport()

            }

        )

    }

}