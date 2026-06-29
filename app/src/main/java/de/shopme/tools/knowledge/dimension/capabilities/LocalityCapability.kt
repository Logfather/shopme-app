package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.report.CoverageDimension

class LocalityCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.LOCALITY

    override val section =
        KnowledgeSection.ORIGIN

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.LOCALITY

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Regionalität",
            description = "Diese Dimension beschreibt die typische räumliche Herkunft eines Lebensmittels.",
            storedFacts = listOf("Herkunftsebene"),
            evaluation = "Der Knowledge Compiler übernimmt Herkunftsinformationen und leitet daraus eine Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Regional", "Das Lebensmittel stammt typischerweise aus der Region."),
                KnowledgeDimensionInterpretation("🟩", "Deutschlandweit", "Das Lebensmittel stammt typischerweise aus Deutschland."),
                KnowledgeDimensionInterpretation("🟡", "Europa", "Das Lebensmittel stammt typischerweise aus Europa."),
                KnowledgeDimensionInterpretation("🔴", "Übersee", "Das Lebensmittel stammt typischerweise aus Übersee.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val locality = knowledge.locality ?: return unknownResult()

        val indicator =
            when (locality) {
                Locality.REGIONAL -> KnowledgeIndicator.GREEN
                Locality.NATIONWIDE -> KnowledgeIndicator.LIGHTGREEN
                Locality.EUROPE -> KnowledgeIndicator.YELLOW
                Locality.OVERSEAS -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = locality.displayName,
            recommendation = "Die Hivra-Einordnung zeigt, wie nah die typische Herkunft des Lebensmittels liegt."
        )
    }
}