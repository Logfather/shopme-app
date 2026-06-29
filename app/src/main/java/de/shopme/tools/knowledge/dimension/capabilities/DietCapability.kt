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

class DietCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.DIET

    override val section =
        KnowledgeSection.CONTENT

    override val order =
        30

    override val coverageDimension =
        CoverageDimension.DIET

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Ernährungsformen",
            description = "Diese Dimension beschreibt, zu welchen Ernährungsformen ein Lebensmittel passt.",
            storedFacts = listOf("Vegan", "Vegetarisch", "Pescetarisch", "Halal", "Koscher"),
            evaluation = "Der Knowledge Compiler klassifiziert Lebensmittel anhand objektiver Eigenschaften und bekannter Zutaten.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟩", "Klassifiziert", "Für das Lebensmittel liegen Ernährungsform-Klassifikationen vor."),
                KnowledgeDimensionInterpretation("⚪", "Unbekannt", "Für das Lebensmittel liegen keine Ernährungsform-Daten vor.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val diets = knowledge.dietClassifications

        if (diets.isEmpty()) return unknownResult()

        return KnowledgeDimensionResult(
            indicator = KnowledgeIndicator.LIGHTGREEN,
            summary = diets.joinToString { it.name },
            recommendation = "Diese Klassifikation beschreibt passende Ernährungsformen und ist keine Qualitätsbewertung."
        )
    }
}