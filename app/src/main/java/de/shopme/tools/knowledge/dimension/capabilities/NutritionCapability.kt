package de.shopme.tools.knowledge.dimension

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.report.CoverageDimension

class NutritionCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id =
        KnowledgeDimensionId.NUTRITION

    override val section =
        KnowledgeSection.CONTENT

    override val order =
        10

    override val coverageDimension =
        CoverageDimension.NUTRITION

    override fun info() =

        KnowledgeDimensionInfo(

            title =

                "Ernährung",

            description =

                "Die Ernährungsdimension beschreibt die ernährungsphysiologische Qualität eines Lebensmittels anhand objektiver Nährwerte.",

            storedFacts = listOf(

                "Kalorien",

                "Eiweiß",

                "Fett",

                "Gesättigte Fettsäuren",

                "Kohlenhydrate",

                "Zucker",

                "Ballaststoffe",

                "Salz"

            ),

            evaluation =

                "Die Rohdaten werden vom Knowledge Compiler übernommen und anschließend in eine Nutri-Score-Klassifikation überführt.",

            interpretations = listOf(

                KnowledgeDimensionInterpretation(

                    indicator = "🟢",

                    title = "Sehr günstig",

                    description =

                        "Das Lebensmittel besitzt eine sehr gute ernährungsphysiologische Qualität."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🟡",

                    title = "Ausgewogen",

                    description =

                        "Das Lebensmittel besitzt eine durchschnittliche ernährungsphysiologische Qualität."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🔴",

                    title = "Eher ungünstig",

                    description =

                        "Das Lebensmittel sollte bewusst konsumiert werden."

                )

            )

        )

    override fun result(

        knowledge: FoodKnowledgeEntry

    ) =

        KnowledgeDimensionResult(

            indicator =

                when (knowledge.nutriScore) {

                    de.shopme.tools.knowledge.nutriscore.NutriScore.A ->

                        KnowledgeIndicator.GREEN

                    de.shopme.tools.knowledge.nutriscore.NutriScore.B ->

                        KnowledgeIndicator.LIGHTGREEN

                    de.shopme.tools.knowledge.nutriscore.NutriScore.C ->

                        KnowledgeIndicator.YELLOW

                    de.shopme.tools.knowledge.nutriscore.NutriScore.D ->

                        KnowledgeIndicator.ORANGE

                    de.shopme.tools.knowledge.nutriscore.NutriScore.E ->

                        KnowledgeIndicator.RED

                    null ->

                        KnowledgeIndicator.UNKNOWN

                },

            summary =

                knowledge.nutriScore?.name

                    ?: "Keine Bewertung",

            recommendation =

                "Die Ernährungsbewertung basiert auf den verfügbaren Nährwerten."

        )

}