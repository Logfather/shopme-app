package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.knowledge.production.ProductionMethod
import de.shopme.tools.report.CoverageDimension

class ProductionCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.PRODUCTION


    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.PRODUCTION

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Herstellung",
            description = "Diese Dimension beschreibt typische Herstellungs- und Verarbeitungsverfahren eines Lebensmittels.",
            storedFacts = listOf("Herstellungsmethoden"),
            evaluation = "Der Knowledge Compiler übernimmt bekannte Herstellungsverfahren und leitet daraus eine verständliche Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Sehr natürlich", "Das Lebensmittel ist kaum oder schonend verarbeitet."),
                KnowledgeDimensionInterpretation("🟡", "Mittel", "Das Lebensmittel ist in moderatem Umfang verarbeitet."),
                KnowledgeDimensionInterpretation("🟧", "Stärker verarbeitet", "Das Lebensmittel ist deutlich verarbeitet."),
                KnowledgeDimensionInterpretation("🔴", "Hoch verarbeitet", "Das Lebensmittel ist sehr stark industriell verarbeitet.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val methods = knowledge.production
        if (methods.isEmpty()) return unknownResult()

        val indicator =
            methods.map(::indicatorFor).maxBy { it.severity() }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = methods.joinToString { it.name },
            recommendation = "Die Hivra-Einordnung basiert auf den bekannten Herstellungs- und Verarbeitungsschritten."
        )
    }

    private fun indicatorFor(method: ProductionMethod): KnowledgeIndicator =
        when (method) {
            ProductionMethod.RAW -> KnowledgeIndicator.GREEN
            ProductionMethod.FERMENTED -> KnowledgeIndicator.LIGHTGREEN
            ProductionMethod.DRIED -> KnowledgeIndicator.LIGHTGREEN
            ProductionMethod.BOILED -> KnowledgeIndicator.LIGHTGREEN
            ProductionMethod.BAKED -> KnowledgeIndicator.YELLOW
            ProductionMethod.FROZEN -> KnowledgeIndicator.YELLOW
            ProductionMethod.FRIED -> KnowledgeIndicator.ORANGE
            ProductionMethod.SMOKED -> KnowledgeIndicator.ORANGE
            ProductionMethod.CANNED -> KnowledgeIndicator.ORANGE
            ProductionMethod.PROCESSED -> KnowledgeIndicator.ORANGE
            ProductionMethod.ULTRA_PROCESSED -> KnowledgeIndicator.RED
        }

    private fun KnowledgeIndicator.severity(): Int =
        when (this) {
            KnowledgeIndicator.GREEN -> 1
            KnowledgeIndicator.LIGHTGREEN -> 2
            KnowledgeIndicator.YELLOW -> 3
            KnowledgeIndicator.ORANGE -> 4
            KnowledgeIndicator.RED -> 5
            KnowledgeIndicator.UNKNOWN -> 0
        }
}