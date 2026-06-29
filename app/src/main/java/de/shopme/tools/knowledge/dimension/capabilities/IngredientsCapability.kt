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

class IngredientsCapability : AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.INGREDIENT_GRAPH


    override val section =
        KnowledgeSection.CONTENT

    override val order =
        40

    override val coverageDimension =
        CoverageDimension.INGREDIENT

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Zutatenliste",
            description = "Diese Dimension beschreibt bekannte Zutaten eines Lebensmittels oder Produkts.",
            storedFacts = listOf("Zutatenreferenzen"),
            evaluation = "Der Knowledge Compiler übernimmt bekannte Zutatenbeziehungen und macht sie für Analyse und Empfehlungen verfügbar.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟩", "Zutaten bekannt", "Für das Lebensmittel liegen Zutateninformationen vor."),
                KnowledgeDimensionInterpretation("⚪", "Unbekannt", "Für das Lebensmittel liegen noch keine Zutateninformationen vor.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val ingredients = knowledge.ingredients

        if (ingredients.isEmpty()) return unknownResult()

        return KnowledgeDimensionResult(
            indicator = KnowledgeIndicator.LIGHTGREEN,
            summary = ingredients.sorted().joinToString(", "),
            recommendation = "Die Zutatenliste bildet die Grundlage für Rezept-, Allergen- und Ernährungsanalysen."
        )
    }
}