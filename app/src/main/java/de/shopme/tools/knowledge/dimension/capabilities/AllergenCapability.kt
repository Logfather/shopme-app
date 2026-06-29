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

class AllergenCapability :
    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.ALLERGENS

    override val section =
        KnowledgeSection.CONTENT

    override val order =
        25

    override val coverageDimension =
        CoverageDimension.ALLERGEN

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Allergene",
            description = "Die Allergendimension beschreibt, welche bekannten allergenen Stoffe einem Lebensmittel zugeordnet sind.",
            storedFacts = listOf(
                "Enthaltene Allergene",
                "Potenzielle Allergenhinweise",
                "Lebensmittelreferenz"
            ),
            evaluation = "Die Allergene werden aus dem kompilierten Hivra-Wissen übernommen und als produktspezifische Hinweise angezeigt.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation(
                    indicator = "🟢",
                    title = "Keine bekannten Allergene",
                    description = "Für dieses Lebensmittel sind keine bekannten Allergene hinterlegt."
                ),
                KnowledgeDimensionInterpretation(
                    indicator = "🟡",
                    title = "Allergenhinweis vorhanden",
                    description = "Für dieses Lebensmittel sind ein oder mehrere Allergene hinterlegt."
                ),
                KnowledgeDimensionInterpretation(
                    indicator = "⚪",
                    title = "Unbekannt",
                    description = "Für dieses Lebensmittel liegen keine ausreichenden Allergeninformationen vor."
                )
            )
        )

    override fun result(
        knowledge: FoodKnowledgeEntry
    ) =
        KnowledgeDimensionResult(
            indicator =
                when {
                    knowledge.allergens.isEmpty() ->
                        KnowledgeIndicator.GREEN

                    else ->
                        KnowledgeIndicator.YELLOW
                },
            summary =
                if (knowledge.allergens.isEmpty()) {
                    "Keine bekannten Allergene"
                } else {
                    knowledge.allergens.joinToString()
                },
            recommendation =
                if (knowledge.allergens.isEmpty()) {
                    "Für dieses Lebensmittel sind aktuell keine bekannten Allergene hinterlegt."
                } else {
                    "Dieses Lebensmittel enthält bekannte Allergene. Bei Unverträglichkeiten sollte der Hinweis beachtet werden."
                }
        )
}