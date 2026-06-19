package de.shopme.domain.nutrition.service

import de.shopme.domain.nutrition.pipeline.ProductionNutritionPipeline

class NutritionWarmupServiceImpl(

    private val pipeline: ProductionNutritionPipeline

) : NutritionWarmupService {

    override suspend fun warmup(

        reference: String

    ) {

        try {

            pipeline.getNutritionDetail(
                reference
            )

        } catch (_: Exception) {

            // niemals UI beeinflussen

        }
    }
}