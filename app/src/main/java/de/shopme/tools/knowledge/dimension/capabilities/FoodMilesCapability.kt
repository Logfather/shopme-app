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

class FoodMilesCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.FOOD_MILES

    override val section =
        KnowledgeSection.ORIGIN

    override val order =
        20

    override val coverageDimension =
        CoverageDimension.FOODMILES

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Transportwege",
            description = "Diese Dimension beschreibt die typische Transportentfernung eines Lebensmittels.",
            storedFacts = listOf("Transportkilometer"),
            evaluation = "Der Knowledge Compiler übernimmt Transportdistanzen und leitet daraus eine Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Kurze Transportwege", "Das Lebensmittel hat typischerweise kurze Transportwege."),
                KnowledgeDimensionInterpretation("🟩", "Moderate Transportwege", "Das Lebensmittel hat überschaubare Transportwege."),
                KnowledgeDimensionInterpretation("🟡", "Mittlere Transportwege", "Das Lebensmittel hat mittlere Transportwege."),
                KnowledgeDimensionInterpretation("🟧", "Lange Transportwege", "Das Lebensmittel hat lange Transportwege."),
                KnowledgeDimensionInterpretation("🔴", "Sehr lange Transportwege", "Das Lebensmittel hat sehr lange Transportwege.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val kilometers = knowledge.foodMiles?.kilometers ?: return unknownResult()

        val indicator =
            when {
                kilometers < 100.0 -> KnowledgeIndicator.GREEN
                kilometers < 500.0 -> KnowledgeIndicator.LIGHTGREEN
                kilometers < 1500.0 -> KnowledgeIndicator.YELLOW
                kilometers < 5000.0 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "${kilometers.toInt()} km",
            recommendation = "Die Hivra-Einordnung zeigt, wie weit ein Lebensmittel typischerweise transportiert wird."
        )
    }
}