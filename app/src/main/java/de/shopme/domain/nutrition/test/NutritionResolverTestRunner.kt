package de.shopme.domain.nutrition.test

import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.nutrition.service.NutritionResolver

class NutritionResolverTestRunner(
    private val nutritionResolver: NutritionResolver
) {

    suspend fun run() {

        RuntimeLog.runtime(
            "========== NUTRITION RESOLVER TEST START =========="
        )

        val products = listOf(
            "Nutella",
            "Milch",
            "Butter",
            "Banane",
            "Coca Cola"
        )

        products.forEach { query ->

            val product =
                nutritionResolver.resolve(
                    query
                )

            RuntimeLog.runtime(
                "Nutrition Resolver Test | " +
                        "query=$query | " +
                        "result=${product?.name} | " +
                        "barcode=${product?.barcode} | " +
                        "nutriScore=${product?.nutrition?.nutriScore}"
            )
        }

        RuntimeLog.runtime(
            "========== NUTRITION RESOLVER TEST END =========="
        )
    }
}