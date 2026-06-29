package de.shopme.tools.knowledge.foods.report

data class FoodsKnowledgeCoverageReport(

    val totalFoods: Int,

    val nutrition: Int,
    val carbon: Int,
    val water: Int,
    val waterStress: Int,
    val biodiversity: Int,
    val pollinator: Int,
    val pesticide: Int,

    val production: Int,
    val processing: Int,
    val packaging: Int,
    val locality: Int,
    val foodMiles: Int,
    val fairTrade: Int,
    val animalWelfare: Int,

    val ingredients: Int,
    val allergens: Int,
    val taxonomy: Int,
    val seasonality: Int,

    val dietClassifications: Int,
    val nutriScore: Int,
    val carbonImpact: Int,
    val glycemicIndex: Int,

    val ingredientGraph: Int,
    val recipeGraph: Int,
    val recipes: Int

)