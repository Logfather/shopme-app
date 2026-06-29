package de.shopme.presentation.developer.foodintelligence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.data.datasource.catalog.CatalogLoader
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.tools.knowledge.compiler.DefaultRuntimeFoodKnowledgeCompilerFactory
import de.shopme.tools.knowledge.dimension.DefaultKnowledgeDimensionRegistry
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.reader.LocalCatalogReader
import de.shopme.tools.report.FoodKnowledgeCoverageCalculator
import de.shopme.tools.report.FoodKnowledgeCoverageReport
import de.shopme.tools.report.ReportTimestamp
import de.shopme.ui.icons.oeicons.OttoThinkingIcon
import de.shopme.ui.theme.BrandCreme
import de.shopme.ui.theme.BrandGreen

private fun coverageColor(
    percentage: Double
): Color =
    when {
        percentage < 40.0 -> Color.Red
        percentage < 60.0 -> Color(0xFFFF9800)
        percentage < 90.0 -> Color.Yellow
        else -> Color(0xFF4CAF50)
    }

@Composable
fun FoodIntelligenceScreen(
    onClose: () -> Unit
) {
    var selectedCapability by remember {
        mutableStateOf<KnowledgeDimensionCapability?>(null)
    }

    val registry = remember {
        DefaultKnowledgeDimensionRegistry.create()
    }

    val context = LocalContext.current

    var sort by remember {
        mutableStateOf(
            CoverageSort.KNOWLEDGE
        )
    }

    val statistics = remember(context) {
        val compiler =
            DefaultRuntimeFoodKnowledgeCompilerFactory.create(
                context
            )

        val calculator =
            FoodKnowledgeCoverageCalculator()

        val catalog =
            LocalCatalogReader(
                CatalogLoader(
                    context
                )
            ).read()

        val knowledge =
            catalog.map(
                compiler::compile
            )

        val report =
            FoodKnowledgeCoverageReport(
                generatedAt = ReportTimestamp.now(),
                catalogEntries = catalog.size,
                entries =
                    calculator.calculate(
                        knowledge
                    )
            )

        report.entries.forEach {
            RuntimeLog.runtime(
                "${it.name} -> ${it.covered}"
            )
        }

        FoodKnowledgeStatisticMapper()
            .map(
                report
            )
    }

    val entries = remember(
        statistics,
        sort
    ) {
        when (sort) {
            CoverageSort.KNOWLEDGE ->
                statistics.sortedBy {
                    it.dimension.order
                }

            CoverageSort.ALPHABET ->
                statistics.sortedBy {
                    it.name
                }

            CoverageSort.COVERAGE_DESC ->
                statistics.sortedByDescending {
                    it.percentage
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandCreme
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Schließen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable {
                        onClose()
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttoThinkingIcon(
                    modifier = Modifier.size(60.dp)
                )

                Text(
                    text = "Lebensmittelwissen",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(
                Modifier.height(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandCreme
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Was zeigt diese Übersicht?",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text =
                            "Diese Übersicht zeigt, für wie viele Produkte bereits Informationen " +
                                    "in den einzelnen Wissensbereichen verfügbar sind.\n\n" +
                                    "Je höher die Abdeckung, desto umfassender kann Hivra Fragen " +
                                    "zu diesem Thema beantworten.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider()

                    Text(
                        text = "Beispiel",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text =
                            "🟢 Ernährung          98 %\n\n" +
                                    "🟡 Regionalität       73 %\n\n" +
                                    "🟠 Bestäuber          34 %\n\n" +
                                    "🔴 Wasserstress        4 %\n",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text =
                            "Die Farben zeigen nicht die Qualität der Lebensmittel, " +
                                    "sondern den Ausbaugrad des vorhandenen Wissens.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        sort = CoverageSort.KNOWLEDGE
                    }
                ) {
                    Text("🧠 Wissen")
                }

                TextButton(
                    onClick = {
                        sort = CoverageSort.ALPHABET
                    }
                ) {
                    Text("A-Z ↑")
                }

                TextButton(
                    onClick = {
                        sort = CoverageSort.COVERAGE_DESC
                    }
                ) {
                    Text("Coverage ↓")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            var currentSection: KnowledgeSection? = null

            entries.forEach { statistic ->
                val section = statistic.dimension.section

                if (section != currentSection) {
                    currentSection = section

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        )
                    )

                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandGreen
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            selectedCapability =
                                registry.all()
                                    .firstOrNull {
                                        it.coverageDimension == statistic.dimension
                                    }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statistic.name,
                        modifier = Modifier.weight(1.4f)
                    )

                    val progress =
                        (statistic.covered.toFloat() / statistic.total.toFloat())
                            .coerceIn(
                                0f,
                                1f
                            )

                    LinearProgressIndicator(
                        progress = {
                            progress
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(6.dp)
                            .clip(
                                RoundedCornerShape(50)
                            ),
                        color = coverageColor(
                            statistic.percentage
                        ),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        text = CoverageFormatter.format(
                            statistic
                        ),
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(start = 12.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Generated by Hivra Food Intelligence Compiler",
                style = MaterialTheme.typography.labelMedium
            )
        }

        selectedCapability?.let { capability ->
            KnowledgeDimensionInfoDialog(
                info = capability.info(),
                onDismiss = {
                    selectedCapability = null
                }
            )
        }
    }
}

@Composable
fun KnowledgeDimensionInfoDialog(
    info: KnowledgeDimensionInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = BrandCreme,
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
                text = info.title
            )
        },
        text = {
            Column {
                Text(
                    text = "Worum geht es?",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = info.description
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(
                    text = "Welche Daten speichern wir?",
                    style = MaterialTheme.typography.titleSmall
                )

                info.storedFacts.forEach {
                    Text(
                        text = "✓ $it"
                    )
                }

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(
                    text = "Wie entsteht die Bewertung?",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = info.evaluation
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(
                    text = "Interpretation",
                    style = MaterialTheme.typography.titleSmall
                )

                info.interpretations.forEach {
                    Text(
                        text = "${it.indicator} ${it.title}"
                    )

                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )
                }
            }
        }
    )
}