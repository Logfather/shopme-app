package de.shopme.tools.knowledge.dimension

class NutritionDimensionProvider :

    KnowledgeDimensionProvider {

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

                    title = "Günstig",

                    description =

                        "Die Bewertung weist auf eine günstige ernährungsphysiologische Qualität hin."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🟡",

                    title = "Mittel",

                    description =

                        "Die Bewertung liegt im mittleren Bereich."

                ),

                KnowledgeDimensionInterpretation(

                    indicator = "🔴",

                    title = "Ungünstig",

                    description =

                        "Die Bewertung weist auf eine eher ungünstige ernährungsphysiologische Qualität hin."

                )

            )

        )

}