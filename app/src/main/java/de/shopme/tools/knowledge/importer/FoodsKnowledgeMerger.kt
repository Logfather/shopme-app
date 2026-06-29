package de.shopme.tools.knowledge.foods.importer

import de.shopme.tools.knowledge.foods.FoodKnowledgeSourceEntry
import de.shopme.tools.knowledge.foods.FoodKnowledgeSources
import de.shopme.tools.knowledge.foods.FoodsKnowledge

class FoodsKnowledgeMerger {

    fun merge(
        base: FoodsKnowledge,
        incoming: FoodsKnowledge
    ): FoodsKnowledge {

        val incomingById =
            incoming.foods
                .associateBy {
                    it.id
                }

        val mergedFoods =
            base.foods
                .map { baseFood ->

                    val incomingFood =
                        incomingById[baseFood.id]
                            ?: return@map baseFood

                    mergeFood(
                        base = baseFood,
                        incoming = incomingFood
                    )
                }

        return FoodsKnowledge(
            version = base.version,
            foods =
                mergedFoods
                    .sortedBy {
                        it.id
                    }
        )
    }

    private fun mergeFood(
        base: FoodKnowledgeSourceEntry,
        incoming: FoodKnowledgeSourceEntry
    ): FoodKnowledgeSourceEntry {

        return base.copy(
            names =
                base.names.copy(
                    aliases =
                        (
                                base.names.aliases +
                                        incoming.names.aliases
                                )
                            .distinct()
                            .sorted()
                ),
            knowledge =
                mergeKnowledge(
                    base = base.knowledge,
                    incoming = incoming.knowledge
                )
        )
    }

    private fun mergeKnowledge(
        base: FoodKnowledgeSources,
        incoming: FoodKnowledgeSources
    ): FoodKnowledgeSources {

        return base.copy(
            nutrition =
                base.nutrition ?: incoming.nutrition,

            carbon =
                base.carbon ?: incoming.carbon,

            water =
                base.water ?: incoming.water,

            waterStress =
                base.waterStress ?: incoming.waterStress,

            biodiversity =
                base.biodiversity ?: incoming.biodiversity,

            pollinator =
                base.pollinator ?: incoming.pollinator,

            pesticide =
                base.pesticide ?: incoming.pesticide,

            production =
                base.production ?: incoming.production,

            processing =
                base.processing ?: incoming.processing,

            packaging =
                base.packaging ?: incoming.packaging,

            locality =
                base.locality ?: incoming.locality,

            foodMiles =
                base.foodMiles ?: incoming.foodMiles,

            fairTrade =
                base.fairTrade ?: incoming.fairTrade,

            animalWelfare =
                base.animalWelfare ?: incoming.animalWelfare,

            ingredients =
                base.ingredients ?: incoming.ingredients,

            allergens =
                base.allergens ?: incoming.allergens,

            taxonomy =
                base.taxonomy ?: incoming.taxonomy,

            seasonality =
                base.seasonality ?: incoming.seasonality,

            dietClassifications =
                base.dietClassifications ?: incoming.dietClassifications,

            nutriScore =
                base.nutriScore ?: incoming.nutriScore,

            carbonImpact =
                base.carbonImpact ?: incoming.carbonImpact,

            glycemicIndex =
                base.glycemicIndex ?: incoming.glycemicIndex,

            ingredientGraph =
                base.ingredientGraph ?: incoming.ingredientGraph,

            recipeGraph =
                base.recipeGraph ?: incoming.recipeGraph,

            recipes =
                base.recipes ?: incoming.recipes
        )
    }
}