package de.shopme.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.presentation.developer.foodintelligence.KnowledgeDimensionInfoDialog
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.explorer.KnowledgeExplorerDimension
import de.shopme.tools.knowledge.dimension.explorer.KnowledgeExplorerModel

@Composable
fun KnowledgeExplorerDialog(

    model: KnowledgeExplorerModel,

    onDismiss: () -> Unit

) {

    var expandedSections by remember {
        mutableStateOf(
            emptySet<KnowledgeSection>()
        )
    }

    var selectedInfo by remember {
        mutableStateOf<KnowledgeDimensionInfo?>(null)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Schließen")
            }
        },

        title = {
            Text(
                text = "Detailinformationen zu ${model.productName}"
            )
        },

        text = {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
            ) {

                model.nutriScore?.let { nutriScore ->

                    KnowledgeExplorerNutriScoreRow(
                        indicator = nutriScore.indicator,
                        summary = nutriScore.summary
                    )
                }

                model.sections.forEach { section ->

                    val expanded =
                        expandedSections.contains(section.section)

                    KnowledgeExplorerSectionRow(
                        section = section.section,
                        expanded = expanded,
                        onClick = {

                            expandedSections =

                                if (expanded) {

                                    expandedSections - section.section

                                } else {

                                    expandedSections + section.section

                                }
                        }
                    )

                    if (expanded) {
                        section.dimensions.forEach { dimension ->

                            KnowledgeExplorerDimensionRow(
                                dimension = dimension,
                                onClick = {
                                    selectedInfo = dimension.info
                                }
                            )
                        }
                    }
                }
            }
        }
    )

    selectedInfo?.let { info ->

        KnowledgeDimensionInfoDialog(
            info = info,
            onDismiss = {
                selectedInfo = null
            }
        )
    }
}

@Composable
private fun KnowledgeExplorerNutriScoreRow(

    indicator: KnowledgeIndicator,

    summary: String

) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
    ) {

        Text(
            text = indicator.toEmoji()
        )

        Spacer(
            Modifier.width(8.dp)
        )

        Text(
            text = "Nutri-Score: $summary"
        )
    }
}

@Composable
private fun KnowledgeExplorerSectionRow(

    section: KnowledgeSection,

    expanded: Boolean,

    onClick: () -> Unit

) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp)
    ) {

        Text(
            text =
                if (expanded) {
                    "▼"
                } else {
                    "▶"
                }
        )

        Spacer(
            Modifier.width(8.dp)
        )

        Text(
            text = section.title
        )
    }
}

@Composable
private fun KnowledgeExplorerDimensionRow(

    dimension: KnowledgeExplorerDimension,

    onClick: () -> Unit

) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    start = 24.dp,
                    top = 6.dp,
                    bottom = 6.dp
                )
    ) {

        Text(
            text = dimension.result.indicator.toEmoji()
        )

        Spacer(
            Modifier.width(8.dp)
        )

        Text(
            text = dimension.info.title
        )
    }
}

private fun KnowledgeIndicator.toEmoji(): String =
    when (this) {
        KnowledgeIndicator.GREEN -> "🟢"
        KnowledgeIndicator.LIGHTGREEN -> "🟢"
        KnowledgeIndicator.YELLOW -> "🟡"
        KnowledgeIndicator.ORANGE -> "🟠"
        KnowledgeIndicator.RED -> "🔴"
        KnowledgeIndicator.UNKNOWN -> "⚪"
    }