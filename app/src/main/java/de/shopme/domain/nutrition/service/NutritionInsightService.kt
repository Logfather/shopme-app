package de.shopme.domain.nutrition.service

import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.domain.nutrition.model.NutritionInsight

interface NutritionInsightService {

    suspend fun getInsight(

        detail: NutritionDetail

    ): NutritionInsight

}