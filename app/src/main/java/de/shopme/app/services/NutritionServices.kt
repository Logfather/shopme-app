package de.shopme.app.services

import de.shopme.domain.nutrition.pipeline.ProductionNutritionPipeline
import de.shopme.domain.nutrition.service.NutritionInsightService

data class NutritionServices(

    val pipeline: ProductionNutritionPipeline,

    val insightService: NutritionInsightService

)