package de.shopme.tools.knowledge.foods

import de.shopme.domain.food.FoodKnowledgeEntry

class FoodsKnowledgeGenerator {

    fun generate(
        entries: List<FoodKnowledgeEntry>
    ): FoodsKnowledge {

        val foods =
            entries
                .map { entry ->

                    FoodKnowledgeSourceEntry(

                        id = entry.normalizedName,

                        names = FoodNames(

                            canonical = entry.normalizedName,

                            aliases = emptyList()

                        ),

                        knowledge = FoodKnowledgeSources(

                            nutrition = entry.nutritionReference?.let { reference ->

                                FoodKnowledgeSource(

                                    reference = reference,

                                    source = "catalog",

                                    value = entry.nutritionFacts

                                )

                            },

                            carbon = entry.nutritionReference?.let { reference ->

                                FoodKnowledgeSource(

                                    reference = reference,

                                    source = "agribalyse",

                                    value = entry.carbonFootprint

                                )

                            },

                            water = entry.waterFootprint?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "water_footprint_network",

                                    value = value

                                )

                            },

                            waterStress = entry.waterStress?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "water_stress",

                                    value = value

                                )

                            },

                            biodiversity = entry.biodiversity?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "biodiversity",

                                    value = value

                                )

                            },

                            pollinator = entry.pollinator?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "pollinator",

                                    value = value

                                )

                            },

                            pesticide = entry.pesticide?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "pesticides",

                                    value = value

                                )

                            },

                            production = entry.production
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "production",

                                        value = value

                                    )

                                },

                            processing = entry.processing?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "processing",

                                    value = value

                                )

                            },

                            packaging = entry.packaging?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "packaging",

                                    value = value

                                )

                            },

                            locality = entry.locality?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "locality",

                                    value = value

                                )

                            },

                            foodMiles = entry.foodMiles?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "food_miles",

                                    value = value

                                )

                            },

                            fairTrade = entry.fairTrade?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "fair_trade",

                                    value = value

                                )

                            },

                            animalWelfare = entry.animalWelfare?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "animal_welfare",

                                    value = value

                                )

                            },

                            ingredients = entry.ingredients
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "ingredients",

                                        value = value

                                    )

                                },

                            allergens = entry.allergens
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "allergens",

                                        value = value

                                    )
                                },

                            taxonomy = entry.taxonomyPath
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "taxonomy",

                                        value = value

                                    )

                                },

                            seasonality = entry.seasonality
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "seasonality",

                                        value = value

                                    )

                                },

                            dietClassifications = entry.dietClassifications
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "diet_classification",

                                        value = value

                                    )

                                },

                            nutriScore = entry.nutriScore?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "nutri_score",

                                    value = value

                                )

                            },

                            carbonImpact = entry.carbonImpact?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "carbon_impact",

                                    value = value

                                )

                            },

                            glycemicIndex = FoodKnowledgeSource(
                                reference = entry.normalizedName,
                                source = "glycemic_index",
                                value = entry.glycemicIndex
                            ),

                            ingredientGraph = entry.ingredientGraph?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "ingredient_graph",

                                    value = value

                                )

                            },

                            recipeGraph = entry.recipeGraph?.let { value ->

                                FoodKnowledgeSource(

                                    reference = entry.normalizedName,

                                    source = "recipe_graph",

                                    value = value

                                )

                            },

                            recipes = entry.recipes
                                .takeIf { it.isNotEmpty() }
                                ?.let { value ->

                                    FoodKnowledgeSource(

                                        reference = entry.normalizedName,

                                        source = "recipes",

                                        value = value

                                    )

                                }
                        )

                    )

                }
                .distinctBy {
                    it.id
                }
                .sortedBy {
                    it.id
                }

        return FoodsKnowledge(

            version = 1,

            foods = foods

        )
    }
}