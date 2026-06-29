package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.knowledge.processing.ProcessingLevel
import de.shopme.tools.report.CoverageDimension

class ProcessingCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.PROCESSING

    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        20

    override val coverageDimension =
        CoverageDimension.PROCESSING

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Verarbeitung",
            description = "Diese Dimension beschreibt den Verarbeitungsgrad eines Lebensmittels nach dem NOVA-Prinzip.",
            storedFacts = listOf("NOVA-Verarbeitungsstufe"),
            evaluation = "Der Knowledge Compiler übernimmt die Verarbeitungsstufe und leitet daraus eine Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Unverarbeitet", "Das Lebensmittel ist unverarbeitet oder minimal verarbeitet."),
                KnowledgeDimensionInterpretation("🟩", "Verarbeitete Zutat", "Das Lebensmittel ist eine einfache verarbeitete Zutat."),
                KnowledgeDimensionInterpretation("🟡", "Verarbeitet", "Das Lebensmittel ist verarbeitet."),
                KnowledgeDimensionInterpretation("🔴", "Hoch verarbeitet", "Das Lebensmittel ist hoch verarbeitet.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val processing = knowledge.processing ?: return unknownResult()

        val indicator =
            when (processing) {
                ProcessingLevel.NOVA_1 -> KnowledgeIndicator.GREEN
                ProcessingLevel.NOVA_2 -> KnowledgeIndicator.LIGHTGREEN
                ProcessingLevel.NOVA_3 -> KnowledgeIndicator.YELLOW
                ProcessingLevel.NOVA_4 -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = processing.displayName,
            recommendation = "Die Hivra-Einordnung ergänzt die NOVA-Stufe um eine leicht verständliche Bewertung."
        )
    }
}