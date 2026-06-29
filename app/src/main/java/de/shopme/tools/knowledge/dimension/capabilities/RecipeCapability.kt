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

class RecipeCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.RECIPE

    override val section =
        KnowledgeSection.RELATIONSHIP

    override val order =
        20

    override val coverageDimension =
        CoverageDimension.RECIPE

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Rezeptliste",
            description = "Diese Dimension beschreibt bekannte Rezepte, in denen ein Lebensmittel verwendet wird.",
            storedFacts = listOf("Rezeptreferenzen"),
            evaluation = "Der Knowledge Compiler verknüpft Lebensmittel mit bekannten Rezepten und macht diese Beziehungen für Planung und Empfehlungen nutzbar.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟩", "Rezepte bekannt", "Für das Lebensmittel liegen Rezeptbeziehungen vor."),
                KnowledgeDimensionInterpretation("⚪", "Unbekannt", "Für das Lebensmittel liegen noch keine Rezeptbeziehungen vor.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val recipes = knowledge.recipes

        if (recipes.isEmpty()) return unknownResult()

        return KnowledgeDimensionResult(
            indicator = KnowledgeIndicator.LIGHTGREEN,
            summary = recipes.sorted().joinToString(", "),
            recommendation = "Rezeptbeziehungen ermöglichen künftig Kochplanung, Einkaufsvorschläge und automatische Listenbildung."
        )
    }
}