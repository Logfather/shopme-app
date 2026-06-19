package de.shopme.data.nutrition.localfallback

import de.shopme.domain.nutrition.model.NutritionInfo
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts

object OfflineNutritionInfoProvider {

    fun nutella() =
        NutritionInfo(

            barcode = "embedded-nutella",

            nutriScore = NutriScore.E,

            facts = NutritionFacts(

                calories = 539.0,

                protein = 6.3,

                fat = 30.9,

                saturatedFat = 10.6,

                carbohydrates = 57.5,

                sugar = 56.3,

                fiber = 6.3,

                salt = 0.11

            ),

            lastUpdated = 0L

        )

    fun cocaCola() =
        NutritionInfo(

            barcode = "embedded-coca-cola",

            nutriScore = NutriScore.E,

            facts = NutritionFacts(

                calories = 42.0,

                protein = 0.0,

                fat = 0.0,

                saturatedFat = 0.0,

                carbohydrates = 10.6,

                sugar = 10.6,

                fiber = 0.0,

                salt = 0.02

            ),

            lastUpdated = 0L

        )

    fun apfel() =
        NutritionInfo(

            barcode = "embedded-apfel",

            nutriScore = NutriScore.A,

            facts = NutritionFacts(

                calories = 52.0,

                protein = 0.3,

                fat = 0.2,

                saturatedFat = 0.0,

                carbohydrates = 13.8,

                sugar = 10.4,

                fiber = 2.4,

                salt = 0.0

            ),

            lastUpdated = 0L

        )

    fun banane() =
        NutritionInfo(

            barcode = "embedded-banane",

            nutriScore = NutriScore.A,

            facts = NutritionFacts(

                calories = 89.0,

                protein = 1.1,

                fat = 0.3,

                saturatedFat = 0.1,

                carbohydrates = 22.8,

                sugar = 12.2,

                fiber = 2.6,

                salt = 0.0

            ),

            lastUpdated = 0L

        )

    fun butter() =
        NutritionInfo(

            barcode = "embedded-butter",

            nutriScore = NutriScore.D,

            facts = NutritionFacts(

                calories = 717.0,

                protein = 0.9,

                fat = 81.0,

                saturatedFat = 51.0,

                carbohydrates = 0.7,

                sugar = 0.7,

                fiber = 0.0,

                salt = 0.02

            ),

            lastUpdated = 0L

        )

    fun vollmilch() =
        NutritionInfo(

            barcode = "embedded-vollmilch",

            nutriScore = NutriScore.B,

            facts = NutritionFacts(

                calories = 64.0,

                protein = 3.3,

                fat = 3.6,

                saturatedFat = 2.3,

                carbohydrates = 4.8,

                sugar = 4.8,

                fiber = 0.0,

                salt = 0.10

            ),

            lastUpdated = 0L

        )
}