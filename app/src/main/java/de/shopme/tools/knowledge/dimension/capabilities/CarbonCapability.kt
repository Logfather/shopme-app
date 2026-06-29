package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.carbon.CarbonImpactLevel
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.report.CoverageDimension

class CarbonCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.CARBON

    override val section =
        KnowledgeSection.IMPACT

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.CARBON

    override fun info() =

        KnowledgeDimensionInfo(

            title =

                "CO₂-Fußabdruck",

            description =

                "Der CO₂-Fußabdruck beschreibt die Menge an Treibhausgasen, die bei Herstellung, Verarbeitung und Transport eines Lebensmittels entsteht.",

            storedFacts = listOf(

                "kg CO₂e / kg Lebensmittel"

            ),

            evaluation =

                "Der Knowledge Compiler übernimmt objektive Emissionswerte und leitet daraus eine Hivra-Bewertung ab.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(

                    indicator = "🟢",

                    title = "Niedrige Emissionen",

                    description =

                        "Das Lebensmittel verursacht vergleichsweise geringe Treibhausgasemissionen."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🟡",

                    title = "Mittlere Emissionen",

                    description =

                        "Das Lebensmittel liegt im mittleren Emissionsbereich."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🔴",

                    title = "Hohe Emissionen",

                    description =

                        "Das Lebensmittel verursacht vergleichsweise hohe Treibhausgasemissionen."

                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ) =

        KnowledgeDimensionResult(

            indicator =

                when (knowledge.carbonImpact) {

                    CarbonImpactLevel.LOW ->

                        KnowledgeIndicator.GREEN

                    CarbonImpactLevel.MEDIUM ->

                        KnowledgeIndicator.YELLOW

                    CarbonImpactLevel.HIGH ->

                        KnowledgeIndicator.ORANGE

                    CarbonImpactLevel.VERY_HIGH ->

                        KnowledgeIndicator.RED

                    null ->

                        KnowledgeIndicator.UNKNOWN

                },

            summary =

                knowledge.carbonFootprint

                    ?.let {

                        "${it.kilogramsPerKilogram} kg CO₂e / kg"

                    }

                    ?: "Keine Daten verfügbar",

            recommendation =

                "Die Hivra-Bewertung ergänzt den objektiven CO₂-Wert um eine leicht verständliche Einordnung."

        )

}