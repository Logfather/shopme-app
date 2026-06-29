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

class WaterStressCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.WATER_STRESS



    override val section =
        KnowledgeSection.IMPACT

    override val order =
        30

    override val coverageDimension =
        CoverageDimension.WATERSTRESS

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Wasservorräte",
            description = "Diese Dimension beschreibt, wie stark die Wasservorräte in den typischen Herkunftsregionen eines Lebensmittels belastet sind.",
            storedFacts = listOf("Wasserstress-Score"),
            evaluation = "Der Knowledge Compiler übernimmt objektive Wasserstresswerte und leitet daraus eine verständliche Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Geringer Wasserstress", "Die Herstellung ist mit vergleichsweise geringer Belastung regionaler Wasservorräte verbunden."),
                KnowledgeDimensionInterpretation("🟡", "Mittlerer Wasserstress", "Die Herstellung liegt im mittleren Belastungsbereich."),
                KnowledgeDimensionInterpretation("🟧", "Hoher Wasserstress", "Die Herstellung ist mit erhöhter Belastung regionaler Wasservorräte verbunden."),
                KnowledgeDimensionInterpretation("🔴", "Sehr hoher Wasserstress", "Die Herstellung ist mit sehr hoher Belastung regionaler Wasservorräte verbunden.")
            )
        )

    override fun result(
        knowledge: FoodKnowledgeEntry
    ): KnowledgeDimensionResult {

        val score =
            knowledge.waterStress?.score
                ?: return unknownResult()

        val indicator =
            when {
                score < 25.0 -> KnowledgeIndicator.GREEN
                score < 50.0 -> KnowledgeIndicator.YELLOW
                score < 75.0 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "Score ${score.toInt()} / 100",
            recommendation = "Die Hivra-Einordnung zeigt, wie stark die Wasservorräte durch typische Produktionsbedingungen belastet werden."
        )
    }
}