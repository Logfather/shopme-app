package de.shopme.domain.nutrition.repository

import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.model.NutritionSearchResult
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {

    suspend fun getProductByBarcode(
        barcode: String
    ): NutritionProduct?

    suspend fun searchProducts(
        query: String
    ): List<NutritionSearchResult>

    suspend fun refreshProduct(
        barcode: String
    ): NutritionProduct?

    suspend fun saveProduct(
        product: NutritionProduct
    )

    suspend fun getOrFetchProduct(
        barcode: String
    ): NutritionProduct? {

        getProductByBarcode(barcode)?.let {
            return it
        }

        return refreshProduct(barcode)
    }

    suspend fun getProductByReference(
        reference: String
    ): NutritionProduct?

    fun observeProduct(
        barcode: String
    ): Flow<NutritionProduct?>

    suspend fun saveReferenceMapping(
        reference: String,
        barcode: String
    )
}