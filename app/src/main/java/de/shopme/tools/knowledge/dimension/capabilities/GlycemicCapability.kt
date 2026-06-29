package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.report.CoverageDimension

class GlycemicCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.GLYCEMIC

    override val section =
        KnowledgeSection.CONTENT

    override val order =
        20

    override val coverageDimension =
        CoverageDimension.GLYCEMIC

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Glykämischer Index",
            description = "Diese Dimension beschreibt, wie stark ein Lebensmittel den Blutzuckerspiegel typischerweise beeinflusst.",
            storedFacts = listOf("Glykämische Einordnung"),
            evaluation = "Der Knowledge Compiler übernimmt die glykämische Einstufung und leitet daraus eine verständliche Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Niedrig", "Das Lebensmittel beeinflusst den Blutzucker typischerweise gering."),
                KnowledgeDimensionInterpretation("🟡", "Mittel", "Das Lebensmittel liegt im mittleren Bereich."),
                KnowledgeDimensionInterpretation("🔴", "Hoch", "Das Lebensmittel kann den Blutzucker stärker beeinflussen.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val level = knowledge.glycemicIndex

        val indicator =
            when (level) {
                GlycemicIndexLevel.LOW -> KnowledgeIndicator.GREEN
                GlycemicIndexLevel.MEDIUM -> KnowledgeIndicator.YELLOW
                GlycemicIndexLevel.HIGH -> KnowledgeIndicator.RED
                GlycemicIndexLevel.UNKNOWN -> KnowledgeIndicator.UNKNOWN
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = level.name,
            recommendation = "Die Hivra-Einordnung ergänzt die glykämische Einstufung um eine leicht verständliche Bewertung."
        )
    }
}