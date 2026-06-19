package de.shopme.domain.nutrition.usecase

import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.repository.NutritionRepository

class RefreshNutritionProductUseCase(
    private val repository: NutritionRepository
) {

    suspend operator fun invoke(
        barcode: String
    ): NutritionProduct? {

        return repository.refreshProduct(barcode)
    }
}