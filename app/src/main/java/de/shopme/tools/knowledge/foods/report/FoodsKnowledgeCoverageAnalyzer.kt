package de.shopme.tools.knowledge.foods.report

import de.shopme.tools.knowledge.foods.FoodsKnowledge

class FoodsKnowledgeCoverageAnalyzer {

    fun analyze(
        knowledge: FoodsKnowledge
    ): FoodsKnowledgeCoverageReport {

        val foods =
            knowledge.foods

        return FoodsKnowledgeCoverageReport(

            totalFoods = foods.size,

            nutrition = foods.count { it.knowledge.nutrition?.value != null },
            carbon = foods.count { it.knowledge.carbon?.value != null },
            water = foods.count { it.knowledge.water?.value != null },
            waterStress = foods.count { it.knowledge.waterStress?.value != null },
            biodiversity = foods.count { it.knowledge.biodiversity?.value != null },
            pollinator = foods.count { it.knowledge.pollinator?.value != null },
            pesticide = foods.count { it.knowledge.pesticide?.value != null },

            production = foods.count { it.knowledge.production?.value?.isNotEmpty() == true },
            processing = foods.count { it.knowledge.processing?.value != null },
            packaging = foods.count { it.knowledge.packaging?.value != null },
            locality = foods.count { it.knowledge.locality?.value != null },
            foodMiles = foods.count { it.knowledge.foodMiles?.value != null },
            fairTrade = foods.count { it.knowledge.fairTrade?.value != null },
            animalWelfare = foods.count { it.knowledge.animalWelfare?.value != null },

            ingredients = foods.count { it.knowledge.ingredients?.value?.isNotEmpty() == true },
            allergens = foods.count { it.knowledge.allergens?.value?.isNotEmpty() == true },
            taxonomy = foods.count { it.knowledge.taxonomy?.value?.isNotEmpty() == true },
            seasonality = foods.count { it.knowledge.seasonality?.value?.isNotEmpty() == true },

            dietClassifications = foods.count { it.knowledge.dietClassifications?.value?.isNotEmpty() == true },
            nutriScore = foods.count { it.knowledge.nutriScore?.value != null },
            carbonImpact = foods.count { it.knowledge.carbonImpact?.value != null },
            glycemicIndex = foods.count { it.knowledge.glycemicIndex?.value != null },

            ingredientGraph = foods.count { it.knowledge.ingredientGraph?.value != null },
            recipeGraph = foods.count { it.knowledge.recipeGraph?.value != null },
            recipes = foods.count { it.knowledge.recipes?.value?.isNotEmpty() == true }

        )
    }
}