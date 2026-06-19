package de.shopme.tools.knowledge.database

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.allergen.AllergenResolver
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonFootprintResolver
import de.shopme.tools.knowledge.glycemic.GlycemicIndexResolver
import de.shopme.tools.knowledge.nutrition.NutritionAliasResolver
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.nutrition.NutritionFactsResolver
import de.shopme.tools.knowledge.seasonality.SeasonalityResolver
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyEntry
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyResolver

class DefaultFoodKnowledgeDatabase(

    private val nutritionFactsResolver: NutritionFactsResolver,
    private val allergenResolver: AllergenResolver,
    private val glycemicIndexResolver: GlycemicIndexResolver,
    private val seasonalityResolver: SeasonalityResolver,
    private val carbonFootprintResolver: CarbonFootprintResolver,
    private val foodTaxonomyResolver: FoodTaxonomyResolver,
    private val nutritionAliasResolver: NutritionAliasResolver

) : FoodKnowledgeDatabase {

    override fun nutritionFacts(
        foodReference: String?
    ): NutritionFacts? =
        nutritionFactsResolver.resolve(foodReference)

    override fun allergens(
        foodReference: String?
    ): Set<Allergen> =
        allergenResolver.resolve(foodReference)

    override fun glycemicIndex(
        foodReference: String?
    ): GlycemicIndexLevel =
        glycemicIndexResolver.resolve(foodReference)

    override fun seasonality(
        foodReference: String?
    ): List<Int> =
        seasonalityResolver.resolve(foodReference)

    override fun carbonFootprint(
        foodReference: String?
    ): CarbonFootprint? =
        carbonFootprintResolver.resolve(foodReference)

    override fun taxonomy(
        foodReference: String?
    ): FoodTaxonomyEntry? =
        foodTaxonomyResolver.resolve(foodReference)

    override fun alias(
        foodReference: String?
    ): String? =
        nutritionAliasResolver.resolve(foodReference)
}