package de.shopme.presentation.components.foodintelligence

import de.shopme.ui.icons.TrafficLight

object NutritionResultProvider {

    fun provide(): FoodDimensionResult {

        val dimension = FoodDimensionProvider.find(

            FoodIntelligenceSection.NUTRITION

        )

        return FoodDimensionResult(

            dimension = dimension,

            trafficLight = TrafficLight.GREEN,

            summary =
                "Dein gewähltes Produkt wird in diesem Bereich als sehr gut eingestuft.",

            recommendation =
                "Dieses Produkt eignet sich gut für eine ausgewogene Ernährung."

        )

    }

}