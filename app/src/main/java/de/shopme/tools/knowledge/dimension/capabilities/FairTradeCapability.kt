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

class FairTradeCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.FAIR_TRADE

    override val section =
        KnowledgeSection.PRODUCTION

    override val order =
        50

    override val coverageDimension =
        CoverageDimension.FAIRTRADE

    override fun info() =

        KnowledgeDimensionInfo(

            title = "Fair Trade",

            description =
                "Diese Dimension beschreibt die typischen sozialen und wirtschaftlichen Produktionsbedingungen eines Lebensmittels.",

            storedFacts =
                listOf("Fair-Trade-Score"),

            evaluation =
                "Der Knowledge Compiler übernimmt objektive Fair-Trade-Bewertungen und leitet daraus eine Hivra-Einordnung ab.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(
                    "🟢",
                    "Sehr fair",
                    "Das Lebensmittel erfüllt hohe Fair-Trade-Standards."
                ),

                KnowledgeDimensionInterpretation(
                    "🟡",
                    "Teilweise fair",
                    "Das Lebensmittel liegt im mittleren Bereich."
                ),

                KnowledgeDimensionInterpretation(
                    "🔴",
                    "Wenig Fair-Trade",
                    "Für das Lebensmittel liegen geringe Fair-Trade-Bewertungen vor."
                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ): KnowledgeDimensionResult {

        val score =
            knowledge.fairTrade?.score
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
                "Die Hivra-Einordnung ergänzt den Fair-Trade-Score um eine leicht verständliche Bewertung."

        )

    }

}