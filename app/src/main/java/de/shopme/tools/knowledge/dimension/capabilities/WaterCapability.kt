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

class WaterCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.WATER

    override val section =
        KnowledgeSection.IMPACT

    override val order =
        20

    override val coverageDimension =
        CoverageDimension.WATER

    override fun info() =

        KnowledgeDimensionInfo(

            title = "Wasserverbrauch",

            description =

                "Die Wasserdimension beschreibt, wie viel Wasser durchschnittlich für die Herstellung eines Lebensmittels benötigt wird.",

            storedFacts = listOf(

                "Liter Wasser / kg Lebensmittel"

            ),

            evaluation =

                "Der Knowledge Compiler übernimmt objektive Wasserverbrauchswerte und leitet daraus eine verständliche Hivra-Einordnung ab.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(

                    indicator = "🟢",

                    title = "Niedriger Wasserverbrauch",

                    description =

                        "Das Lebensmittel benötigt vergleichsweise wenig Wasser in der Herstellung."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🟡",

                    title = "Mittlerer Wasserverbrauch",

                    description =

                        "Das Lebensmittel liegt im mittleren Bereich des Wasserverbrauchs."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🟧",

                    title = "Hoher Wasserverbrauch",

                    description =

                        "Das Lebensmittel benötigt vergleichsweise viel Wasser in der Herstellung."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🔴",

                    title = "Sehr hoher Wasserverbrauch",

                    description =

                        "Das Lebensmittel verursacht einen sehr hohen Wasserverbrauch."

                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ): KnowledgeDimensionResult {

        val water =

            knowledge.waterFootprint

                ?: return unknownResult()

        val liters =

            water.litersPerKilogram

        val indicator =

            when {

                liters < 500.0 ->

                    KnowledgeIndicator.GREEN

                liters < 1500.0 ->

                    KnowledgeIndicator.YELLOW

                liters < 5000.0 ->

                    KnowledgeIndicator.ORANGE

                else ->

                    KnowledgeIndicator.RED

            }

        return KnowledgeDimensionResult(

            indicator = indicator,

            summary = "${liters.toInt()} l / kg",

            recommendation =

                "Die Hivra-Einordnung ergänzt den objektiven Wasserverbrauch um eine leicht verständliche Bewertung."

        )

    }

}