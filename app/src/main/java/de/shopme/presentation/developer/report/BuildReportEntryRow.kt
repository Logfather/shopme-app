package de.shopme.presentation.developer.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BuildReportEntryRow(

    entry: BuildReportEntry

) {

    Row(

        modifier =

            Modifier.fillMaxWidth(),

        horizontalArrangement =

            Arrangement.SpaceBetween

    ) {

        Text(

            text = entry.name,

            style =

                MaterialTheme.typography.bodyLarge

        )

        Text(

            text =

                entry.count.toString(),

            style =

                MaterialTheme.typography.bodyLarge

        )

    }

}