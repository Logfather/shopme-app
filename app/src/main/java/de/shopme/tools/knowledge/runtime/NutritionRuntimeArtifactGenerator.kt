package de.shopme.tools.knowledge.foods.runtime

import de.shopme.tools.knowledge.foods.FoodsKnowledge
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge

class NutritionRuntimeArtifactGenerator {

    fun generate(
        foodsKnowledge: FoodsKnowledge
    ): NutritionFactsKnowledge {

        val entries =
            foodsKnowledge.foods
                .mapNotNull { food ->

                    val reference =
                        food.knowledge.nutrition
                            ?.reference
                            ?: food.id

                    val facts =
                        food.knowledge.nutrition
                            ?.value

                    if (facts == null) {
                        null
                    } else {
                        reference to facts
                    }
                }
                .toMap()
                .toSortedMap()

        return NutritionFactsKnowledge(
            entries = entries
        )
    }
}