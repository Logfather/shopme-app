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

class AnimalWelfareCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.ANIMAL_WELFARE



    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        40

    override val coverageDimension =
        CoverageDimension.ANIMALWELFARE

    override fun info() =

        KnowledgeDimensionInfo(

            title = "Tierwohl",

            description =
                "Diese Dimension beschreibt die typischen Haltungs- und Produktionsbedingungen tierischer Lebensmittel.",

            storedFacts =
                listOf("Tierwohl-Score"),

            evaluation =
                "Der Knowledge Compiler übernimmt objektive Tierwohlbewertungen und leitet daraus eine Hivra-Einordnung ab.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(
                    "🟢",
                    "Sehr gutes Tierwohl",
                    "Das Lebensmittel stammt typischerweise aus besonders tiergerechter Haltung."
                ),

                KnowledgeDimensionInterpretation(
                    "🟡",
                    "Mittleres Tierwohl",
                    "Das Lebensmittel liegt im mittleren Bereich."
                ),

                KnowledgeDimensionInterpretation(
                    "🔴",
                    "Geringes Tierwohl",
                    "Das Lebensmittel stammt typischerweise aus weniger tiergerechter Haltung."
                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ): KnowledgeDimensionResult {

        val score =
            knowledge.animalWelfare?.score
                ?: return unknownResult()

        val indicator =

            when {

                score >= 0.8 -> KnowledgeIndicator.GREEN

                score >= 0.6 -> KnowledgeIndicator.LIGHTGREEN

                score >= 0.4 -> KnowledgeIndicator.YELLOW

                score >= 0.2 -> KnowledgeIndicator.ORANGE

                else -> KnowledgeIndicator.RED

            }

        return KnowledgeDimensionResult(

            indicator = indicator,

            summary =

                "Score %.1f".format(score),

            recommendation =
                "Die Hivra-Einordnung ergänzt den Tierwohl-Score um eine leicht verständliche Bewertung."

        )

    }

}