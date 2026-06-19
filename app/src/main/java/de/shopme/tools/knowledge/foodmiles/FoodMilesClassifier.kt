package de.shopme.tools.knowledge.foodmiles

class FoodMilesClassifier {

    fun classify(
        miles: FoodMiles?
    ): FoodMilesLevel {

        if (miles == null) {

            return FoodMilesLevel.REGIONAL

        }

        return when {

            miles.kilometers < 50 ->

                FoodMilesLevel.LOCAL

            miles.kilometers < 300 ->

                FoodMilesLevel.REGIONAL

            miles.kilometers < 1500 ->

                FoodMilesLevel.NATIONAL

            else ->

                FoodMilesLevel.INTERNATIONAL

        }

    }

}