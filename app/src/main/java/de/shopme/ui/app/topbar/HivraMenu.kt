package de.shopme.ui.app.topbar

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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

        DropdownMenuItem(

            text = {

                Text("🧠 Lebensmittelwissen")

            },

            onClick = {

                onDismiss()

                onFoodIntelligence()

            }

        )

//        DropdownMenuItem(
//
//            text = {
//
//                Text("📊 Build Report")
//
//            },
//
//            onClick = {
//
//                onDismiss()
//
//                onBuildReport()
//
//            }
//
//        )

    }

}