package de.shopme.data.nutrition.mapper

import de.shopme.data.nutrition.local.NutritionInfoEntity
import de.shopme.data.nutrition.local.NutritionProductEntity
import de.shopme.data.nutrition.local.NutritionProductWithInfo
import de.shopme.domain.nutrition.model.NutritionInfo
import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts

object NutritionMapper {

    fun toDomain(
        relation: NutritionProductWithInfo
    ): NutritionProduct {

        return NutritionProduct(

            barcode = relation.product.barcode,

            name = relation.product.name,

            brand = relation.product.brand,

            category = relation.product.category,

            imageUrl = relation.product.imageUrl,

            nutrition = relation.nutrition?.let {

                NutritionInfo(

                    barcode = it.barcode,

                    nutriScore =
                        runCatching {
                            NutriScore.valueOf(
                                it.nutriScore
                            )
                        }.getOrNull(),

                    facts = NutritionFacts(

                        calories =
                            it.caloriesPer100g ?: 0.0,

                        protein =
                            it.proteinPer100g ?: 0.0,

                        fat =
                            it.fatPer100g ?: 0.0,

                        saturatedFat =
                            it.saturatedFatPer100g ?: 0.0,

                        carbohydrates =
                            it.carbohydratesPer100g ?: 0.0,

                        sugar =
                            it.sugarPer100g ?: 0.0,

                        fiber =
                            it.fiberPer100g ?: 0.0,

                        salt =
                            it.saltPer100g ?: 0.0

                    ),

                    lastUpdated = it.updatedAt

                )

            }

        )

    }

    fun toProductEntity(
        product: NutritionProduct
    ): NutritionProductEntity {

        return NutritionProductEntity(

            barcode = product.barcode,

            name = product.name,

            brand = product.brand,

            category = product.category,

            imageUrl = product.imageUrl,

            updatedAt =
                product.nutrition?.lastUpdated
                    ?: System.currentTimeMillis()

        )

    }

    fun toNutritionEntity(
        product: NutritionProduct
    ): NutritionInfoEntity? {

        val nutrition =
            product.nutrition ?: return null

        val facts =
            nutrition.facts

        return NutritionInfoEntity(

            barcode = nutrition.barcode,

            nutriScore =
                nutrition.nutriScore?.name ?: "",

            caloriesPer100g =
                facts.calories,

            fatPer100g =
                facts.fat,

            saturatedFatPer100g =
                facts.saturatedFat,

            carbohydratesPer100g =
                facts.carbohydrates,

            sugarPer100g =
                facts.sugar,

            fiberPer100g =
                facts.fiber,

            proteinPer100g =
                facts.protein,

            saltPer100g =
                facts.salt,

            updatedAt =
                nutrition.lastUpdated

        )
    }

}