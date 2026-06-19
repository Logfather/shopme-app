package de.shopme.domain.recommendation.statistics.counter

import de.shopme.domain.food.FoodCategory
import de.shopme.domain.food.FoodClassifier

class ProteinStatisticsCounter(

    private val foodClassifier: FoodClassifier

) {

    fun count(
        purchases: List<String>
    ): Int {

        return purchases.count { purchase ->

            foodClassifier.classify(
                purchase
            ) == FoodCategory.PROTEIN

        }

    }

}