package de.shopme.data.nutrition.remote

import de.shopme.data.nutrition.mapper.OpenFoodFactsMapper
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.model.NutritionSearchResult

class OpenFoodFactsDataSource {

    private val api =
        OpenFoodFactsClient.api

    suspend fun getProduct(
        barcode: String
    ): NutritionProduct? {

        return runCatching {

            api.getProduct(barcode)
                .product
                ?.let(
                    OpenFoodFactsMapper::toDomain
                )

        }.getOrNull()
    }

    suspend fun searchProducts(
        query: String
    ): List<NutritionSearchResult> {

        return runCatching {

            val response =
                api.searchProducts(query)

            RuntimeLog.runtime(
                "OpenFoodFacts search " +
                        "query=$query " +
                        "results=${response.products?.size ?: 0}"
            )

            RuntimeLog.runtime(
                "RAW Search query=$query " +
                        "products=${response.products?.size ?: 0}"
            )

            val mapped =
                response.products
                    ?.map(
                        OpenFoodFactsMapper::toSearchResult
                    )
                    ?: emptyList()

            RuntimeLog.runtime(
                "MAPPED Search query=$query " +
                        "products=${mapped.size}"
            )

            mapped

        }.onFailure {

            RuntimeLog.runtime(
                "OpenFoodFacts search failed " +
                        "query=$query " +
                        "error=${it.message}"
            )

        }.getOrDefault(
            emptyList()
        )
    }
}