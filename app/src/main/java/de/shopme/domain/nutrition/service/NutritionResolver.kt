package de.shopme.domain.nutrition.service

import de.shopme.domain.nutrition.model.NutritionProduct

interface NutritionResolver {

    suspend fun resolve(
        productName: String
    ): NutritionProduct?
}