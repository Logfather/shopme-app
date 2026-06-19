package de.shopme.tools.knowledge.database

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyEntry

interface FoodKnowledgeDatabase {

    fun nutritionFacts(foodReference: String?): NutritionFacts?

    fun allergens(foodReference: String?): Set<Allergen>

    fun glycemicIndex(foodReference: String?): GlycemicIndexLevel

    fun seasonality(foodReference: String?): List<Int>

    fun carbonFootprint(foodReference: String?): CarbonFootprint?

    fun taxonomy(foodReference: String?): FoodTaxonomyEntry?

    fun alias(foodReference: String?): String?
}