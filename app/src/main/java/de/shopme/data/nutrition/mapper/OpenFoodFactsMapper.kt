package de.shopme.data.nutrition.mapper

import de.shopme.data.nutrition.dto.OpenFoodFactsProductDto
import de.shopme.domain.nutrition.model.NutritionInfo
import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.model.NutritionSearchResult
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts

object OpenFoodFactsMapper {

    fun toDomain(
        dto: OpenFoodFactsProductDto
    ): NutritionProduct {

        return NutritionProduct(

            barcode =
                dto.code.orEmpty(),

            name =
                dto.product_name
                    ?: "Unbekannt",

            brand =
                dto.brands,

            category =
                dto.categories,

            imageUrl =
                dto.image_url,

            nutrition =
                NutritionInfo(

                    barcode =
                        dto.code.orEmpty(),

                    nutriScore =
                        dto.nutriscore_grade
                            .toNutriScore(),

                    facts = NutritionFacts(

                        calories =
                            dto.nutriments
                                ?.energy_kcal_100g ?: 0.0,

                        protein =
                            dto.nutriments
                                ?.proteins_100g ?: 0.0,

                        fat =
                            dto.nutriments
                                ?.fat_100g ?: 0.0,

                        saturatedFat =
                            dto.nutriments
                                ?.saturated_fat_100g ?: 0.0,

                        carbohydrates =
                            dto.nutriments
                                ?.carbohydrates_100g ?: 0.0,

                        sugar =
                            dto.nutriments
                                ?.sugars_100g ?: 0.0,

                        fiber =
                            dto.nutriments
                                ?.fiber_100g ?: 0.0,

                        salt =
                            dto.nutriments
                                ?.salt_100g ?: 0.0

                    ),

                    lastUpdated =
                        System.currentTimeMillis()

                )

        )

    }

    fun toSearchResult(
        dto: OpenFoodFactsProductDto
    ): NutritionSearchResult {

        return NutritionSearchResult(

            barcode =
                dto.code.orEmpty(),

            name =
                dto.product_name
                    ?: "",

            brand =
                dto.brands,

            nutriScore =
                dto.nutriscore_grade
                    .toNutriScore(),

            confidence =
                0f

        )

    }

    private fun String?.toNutriScore(): NutriScore? {

        return runCatching {

            NutriScore.valueOf(
                this?.uppercase() ?: ""
            )

        }.getOrNull()

    }

}