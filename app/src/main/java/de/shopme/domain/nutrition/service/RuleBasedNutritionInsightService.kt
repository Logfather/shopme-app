package de.shopme.domain.nutrition.service

import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.domain.nutrition.model.NutritionInsight

class RuleBasedNutritionInsightService :

    NutritionInsightService {

    override suspend fun getInsight(

        detail: NutritionDetail

    ): NutritionInsight {

        return when (

            detail.nutriScore.uppercase()

        ) {

            "A" ->

                NutritionInsight(

                    title = "🟢 ShopBuddy empfiehlt",

                    text =
                        "Sehr ausgewogene Nährwerte. Dieses Produkt passt hervorragend in den täglichen Einkauf.",

                    source = "rule-engine",

                    generatedAt =
                        System.currentTimeMillis()

                )

            "B" ->

                NutritionInsight(

                    title = "🟢 ShopBuddy empfiehlt",

                    text =
                        "Eine gute Wahl für eine ausgewogene Ernährung.",

                    source = "rule-engine",

                    generatedAt =
                        System.currentTimeMillis()

                )

            "C" ->

                NutritionInsight(

                    title = "🟡 ShopBuddy Hinweis",

                    text =
                        "In Ordnung für den gelegentlichen Genuss. Achte auf eine abwechslungsreiche Auswahl.",

                    source = "rule-engine",

                    generatedAt =
                        System.currentTimeMillis()

                )

            "D" ->

                NutritionInsight(

                    title = "🟠 ShopBuddy Hinweis",

                    text =
                        "Dieses Produkt enthält vergleichsweise viel Fett oder Zucker. Eine Alternative könnte sinnvoll sein.",

                    source = "rule-engine",

                    generatedAt =
                        System.currentTimeMillis()

                )

            else ->

                NutritionInsight(

                    title = "🔴 ShopBuddy Hinweis",

                    text =
                        "Der NutriScore ist ungünstig. Vielleicht findest du eine ausgewogenere Alternative.",

                    source = "rule-engine",

                    generatedAt =
                        System.currentTimeMillis()

                )
        }
    }
}