package de.shopme.presentation.components.foodintelligence

import de.shopme.ui.icons.TrafficLight

object FoodDimensionResultProvider {

    fun find(

        section: FoodIntelligenceSection

    ): FoodDimensionResult {

        val dimension = FoodDimensionProvider.find(

            section

        )

        val trafficLight = when (section) {

            FoodIntelligenceSection.NUTRITION ->
                TrafficLight.GREEN

            FoodIntelligenceSection.CARBON ->
                TrafficLight.YELLOW

            FoodIntelligenceSection.WATER ->
                TrafficLight.GREEN

            FoodIntelligenceSection.PROCESSING ->
                TrafficLight.ORANGE

            FoodIntelligenceSection.PACKAGING ->
                TrafficLight.RED

            FoodIntelligenceSection.BIODIVERSITY ->
                TrafficLight.GREEN

            FoodIntelligenceSection.POLLINATOR ->
                TrafficLight.GREEN

            FoodIntelligenceSection.LOCALITY ->
                TrafficLight.YELLOW

            FoodIntelligenceSection.FAIR_TRADE ->
                TrafficLight.YELLOW


            FoodIntelligenceSection.ANIMAL_WELFARE ->
                TrafficLight.YELLOW

            FoodIntelligenceSection.PESTICIDE ->
                TrafficLight.YELLOW

        }

        return FoodDimensionResult(

            dimension = dimension,

            trafficLight = trafficLight,

            summary =
                "Dein gewähltes Produkt wird in diesem Bereich als ${trafficLight.displayName.lowercase()} eingestuft.",

            recommendation =

                when (trafficLight) {

                    TrafficLight.GREEN ->
                        "Dieses Produkt eignet sich gut für eine ausgewogene Ernährung."

                    TrafficLight.LIGHT_GREEN ->
                        "Dieses Produkt passt gut in eine ausgewogene Ernährung."

                    TrafficLight.YELLOW ->
                        "Dieses Produkt kann bewusst in eine ausgewogene Ernährung integriert werden."

                    TrafficLight.ORANGE ->
                        "Dieses Produkt sollte eher gelegentlich verzehrt werden."

                    TrafficLight.RED ->
                        "Dieses Produkt eignet sich eher für den gelegentlichen Verzehr."

                }

        )

    }

}