package de.shopme.domain.nutrition.mapper

import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.presentation.components.NutritionShopBuddyProvider
import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.tools.knowledge.nutrition.NutritionFacts

object NutritionDetailMapper {

    fun toDetail(
        product: NutritionProduct
    ): NutritionDetail {

        val nutrition =
            product.nutrition

        val nutriScore =
            nutrition?.nutriScore
                ?.name
                ?.uppercase()
                ?: "-"

        return NutritionDetail(

            productName = product.name,

            nutriScore = nutriScore,

            values = NutritionFacts(

                calories =
                    nutrition?.facts?.calories ?: 0.0,

                protein =
                    nutrition?.facts?.protein ?: 0.0,

                fat =
                    nutrition?.facts?.fat ?: 0.0,

                saturatedFat =
                    nutrition?.facts?.saturatedFat ?: 0.0,

                carbohydrates =
                    nutrition?.facts?.carbohydrates ?: 0.0,

                sugar =
                    nutrition?.facts?.sugar ?: 0.0,

                fiber =
                    nutrition?.facts?.fiber ?: 0.0,

                salt =
                    nutrition?.facts?.salt ?: 0.0

            ),

            infoTitle =
                "🟢 Gut zu wissen",

            infoText =
                "Für dieses Produkt liegen Nährwertinformationen vor.",

            buddyState =
                NutritionShopBuddyProvider.get(
                    nutriScore
                )

        )

    }
}