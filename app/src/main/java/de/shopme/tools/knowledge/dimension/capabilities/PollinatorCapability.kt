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

class PollinatorCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.POLLINATOR

    override val section =
        KnowledgeSection.IMPACT

    override val order =
        50

    override val coverageDimension =
        CoverageDimension.POLLINATOR

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Bestäuber",
            description = "Diese Dimension beschreibt, wie stark ein Lebensmittel mit der Bedeutung und Belastung von Bestäubern wie Bienen, Hummeln und anderen Insekten verbunden ist.",
            storedFacts = listOf("Bestäuber-Score"),
            evaluation = "Der Knowledge Compiler übernimmt Bestäuberwerte und leitet daraus eine verständliche Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Sehr günstig", "Das Lebensmittel ist mit einer günstigen Einordnung für Bestäuber verbunden."),
                KnowledgeDimensionInterpretation("🟡", "Mittel", "Das Lebensmittel liegt im mittleren Bereich."),
                KnowledgeDimensionInterpretation("🟧", "Eher ungünstig", "Das Lebensmittel ist mit erhöhter Belastung für Bestäuber verbunden."),
                KnowledgeDimensionInterpretation("🔴", "Ungünstig", "Das Lebensmittel ist mit hoher Belastung für Bestäuber verbunden.")
            )
        )

    override fun result(
        knowledge: FoodKnowledgeEntry
    ): KnowledgeDimensionResult {

        val score =
            knowledge.pollinator?.score
                ?: return unknownResult()

        val indicator =
            when {
                score >= 80.0 -> KnowledgeIndicator.GREEN
                score >= 60.0 -> KnowledgeIndicator.LIGHTGREEN
                score >= 40.0 -> KnowledgeIndicator.YELLOW
                score >= 20.0 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "Score ${score.toInt()} / 100",
            recommendation = "Die Hivra-Einordnung zeigt, wie günstig oder belastend das Lebensmittel für Bestäuber eingeschätzt wird."
        )
    }
}