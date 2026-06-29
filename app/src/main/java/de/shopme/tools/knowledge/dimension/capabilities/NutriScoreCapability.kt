package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.report.CoverageDimension

class NutriScoreCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.NUTRI_SCORE

    override val section =
        KnowledgeSection.INTERPRETATION

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.NUTRISCORE

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Nutri Score",
            description = "Diese Dimension zeigt die offizielle Nutri-Score-Einstufung eines Lebensmittels.",
            storedFacts = listOf("Nutri-Score A bis E"),
            evaluation = "Der Knowledge Compiler übernimmt oder berechnet die Nutri-Score-Einstufung anhand verfügbarer Nährwerte.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "A", "Sehr günstige Nutri-Score-Einstufung."),
                KnowledgeDimensionInterpretation("🟩", "B", "Günstige Nutri-Score-Einstufung."),
                KnowledgeDimensionInterpretation("🟡", "C", "Mittlere Nutri-Score-Einstufung."),
                KnowledgeDimensionInterpretation("🟧", "D", "Eher ungünstige Nutri-Score-Einstufung."),
                KnowledgeDimensionInterpretation("🔴", "E", "Ungünstige Nutri-Score-Einstufung.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val score = knowledge.nutriScore ?: return unknownResult()

        val indicator =
            when (score) {
                NutriScore.A -> KnowledgeIndicator.GREEN
                NutriScore.B -> KnowledgeIndicator.LIGHTGREEN
                NutriScore.C -> KnowledgeIndicator.YELLOW
                NutriScore.D -> KnowledgeIndicator.ORANGE
                NutriScore.E -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = score.name,
            recommendation = "Der Nutri-Score bleibt die primäre offizielle Darstellung; Hivra ergänzt ihn nur um eine einheitliche visuelle Einordnung."
        )
    }
}