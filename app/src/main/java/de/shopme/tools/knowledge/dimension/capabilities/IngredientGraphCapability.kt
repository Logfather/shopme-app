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

class IngredientGraphCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.INGREDIENT_GRAPH

    override val section =
        KnowledgeSection.RELATIONSHIP

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.INGREDIENT

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Zutatenbeziehungen",
            description = "Diese Dimension beschreibt strukturierte Beziehungen zwischen Lebensmitteln und Zutaten.",
            storedFacts = listOf("Ingredient Graph Entry"),
            evaluation = "Der Knowledge Compiler verknüpft Zutaten zu einem Graphen, damit Hivra Zusammenhänge zwischen Lebensmitteln erkennen kann.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟩", "Graph vorhanden", "Für das Lebensmittel liegen strukturierte Zutatenbeziehungen vor."),
                KnowledgeDimensionInterpretation("⚪", "Unbekannt", "Für das Lebensmittel liegen noch keine Zutatenbeziehungen vor.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val graph = knowledge.ingredientGraph ?: return unknownResult()

        return KnowledgeDimensionResult(
            indicator = KnowledgeIndicator.LIGHTGREEN,
            summary = "Zutatenbeziehungen vorhanden",
            recommendation = "Der Ingredient Graph ermöglicht künftig tiefere Analysen wie Rezeptableitung, Austauschvorschläge und Konflikterkennung."
        )
    }
}