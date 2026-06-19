package de.shopme.presentation.developer.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BuildReportCard(

    section: BuildReportSection

) {

    Card(

        modifier =

            Modifier.fillMaxWidth(),

        colors =

            CardDefaults.cardColors()

    ) {

        Column(

            modifier =

                Modifier.padding(16.dp)

        ) {

            Text(

                text = section.title,

                style =

                    MaterialTheme.typography.titleLarge

            )

            section.entries.forEach {

                BuildReportEntryRow(

                    entry = it

                )

            }

        }

    }

}