package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.report.CoverageDimension

class PackagingCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.PACKAGING

    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        30

    override val coverageDimension =
        CoverageDimension.PACKAGING

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Verpackung",
            description = "Diese Dimension beschreibt die Verpackungsqualität eines Lebensmittels aus Sicht von Nachhaltigkeit und Ressourcenschonung.",
            storedFacts = listOf("Verpackungs-Score"),
            evaluation = "Der Knowledge Compiler übernimmt Verpackungswerte und leitet daraus eine Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Sehr günstig", "Die Verpackung ist vergleichsweise nachhaltig."),
                KnowledgeDimensionInterpretation("🟡", "Mittel", "Die Verpackung liegt im mittleren Bereich."),
                KnowledgeDimensionInterpretation("🟧", "Eher ungünstig", "Die Verpackung ist weniger günstig."),
                KnowledgeDimensionInterpretation("🔴", "Ungünstig", "Die Verpackung ist aus Nachhaltigkeitssicht ungünstig.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val score = knowledge.packaging?.score ?: return unknownResult()

        val indicator =
            when {
                score >= 8.0 -> KnowledgeIndicator.GREEN
                score >= 6.0 -> KnowledgeIndicator.LIGHTGREEN
                score >= 4.0 -> KnowledgeIndicator.YELLOW
                score >= 2.0 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "Score ${score.toInt()} / 10",
            recommendation = "Die Hivra-Einordnung zeigt, wie günstig die Verpackung im Vergleich bewertet wird."
        )
    }
}