package de.shopme.data.nutrition.fake

import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.domain.nutrition.model.NutritionInsight
import de.shopme.domain.nutrition.service.NutritionInsightService

class FakeNutritionInsightService : NutritionInsightService {

    override suspend fun getInsight(
        detail: NutritionDetail
    ): NutritionInsight {

        return when (detail.nutriScore.uppercase()) {

            "A" -> NutritionInsight(

                title = "🟢 Gut zu wissen",

                text =
                    "Sehr gute Wahl!\n\n" +
                            "Dieses Produkt passt hervorragend in eine ausgewogene Ernährung " +
                            "und kann regelmäßig auf dem Einkaufszettel stehen.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )

            "B" -> NutritionInsight(

                title = "🟢 Gut zu wissen",

                text =
                    "Eine gute Wahl für den Alltag.\n\n" +
                            "Abwechslungsreiche Lebensmittel sorgen für eine ausgewogene Ernährung.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )

            "C" -> NutritionInsight(

                title = "💡 Gut zu wissen",

                text =
                    "Dieses Produkt liegt im mittleren Bereich.\n\n" +
                            "Es passt gut zu einer abwechslungsreichen Ernährung und kann bewusst ausgewählt werden.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )

            "D" -> NutritionInsight(

                title = "🟠 Gut zu wissen",

                text =
                    "Dieses Produkt enthält vergleichsweise viel Zucker, Fett oder Salz.\n\n" +
                            "Vielleicht findest du eine etwas ausgewogenere Alternative.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )

            "E" -> NutritionInsight(

                title = "🔴 Gut zu wissen",

                text =
                    "Dieses Produkt enthält viel Zucker, Fett oder Salz.\n\n" +
                            "Für den täglichen Einkauf eignen sich weniger stark verarbeitete Alternativen oft besser.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )

            else -> NutritionInsight(

                title = "💡 Gut zu wissen",

                text =
                    "Für dieses Produkt liegen aktuell noch keine weiteren Informationen vor.",

                source = "fake",

                generatedAt = System.currentTimeMillis()
            )
        }
    }
}