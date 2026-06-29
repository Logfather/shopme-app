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

class PesticideCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.PESTICIDES

    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        60

    override val coverageDimension =
        CoverageDimension.PESTICIDE

    override fun info() =

        KnowledgeDimensionInfo(

            title = "Pestizide",

            description =
                "Diese Dimension beschreibt die typische Belastung eines Lebensmittels mit Pestizidrückständen.",

            storedFacts =
                listOf("Pestizid-Score"),

            evaluation =
                "Der Knowledge Compiler übernimmt bekannte Belastungswerte und leitet daraus eine verständliche Hivra-Einordnung ab.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(
                    "🟢",
                    "Niedrige Belastung",
                    "Das Lebensmittel weist typischerweise geringe Pestizidrückstände auf."
                ),

                KnowledgeDimensionInterpretation(
                    "🟡",
                    "Mittlere Belastung",
                    "Das Lebensmittel liegt im mittleren Bereich."
                ),

                KnowledgeDimensionInterpretation(
                    "🔴",
                    "Hohe Belastung",
                    "Das Lebensmittel weist typischerweise erhöhte Pestizidrückstände auf."
                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ): KnowledgeDimensionResult {

        val score =
            knowledge.pesticide?.score
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
                "Die Hivra-Einordnung ergänzt den objektiven Pestizid-Score um eine leicht verständliche Bewertung."

        )

    }

}