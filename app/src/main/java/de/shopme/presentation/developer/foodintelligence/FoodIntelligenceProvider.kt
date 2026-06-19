package de.shopme.presentation.developer.foodintelligence

import android.content.Context

interface FoodIntelligenceProvider {

    fun statistics(

        context: Context

    ): List<FoodKnowledgeStatistic>

}