package de.shopme.domain.nutrition.service

interface NutritionWarmupService {

    suspend fun warmup(

        reference: String

    )

}