package de.shopme.tools.knowledge.foods

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfare
import de.shopme.tools.knowledge.biodiversity.BiodiversityScore
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonImpactLevel
import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.fairtrade.FairTrade
import de.shopme.tools.knowledge.foodmiles.FoodMiles
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphEntry
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.packaging.Packaging
import de.shopme.tools.knowledge.pesticides.Pesticide
import de.shopme.tools.knowledge.pollinator.PollinatorScore
import de.shopme.tools.knowledge.processing.ProcessingLevel
import de.shopme.tools.knowledge.production.ProductionMethod
import de.shopme.tools.knowledge.recipegraph.RecipeGraphEntry
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterstress.WaterStress

object EmptyFoodLookup : FoodLookup {

    override fun findById(
        id: String
    ): FoodKnowledgeSourceEntry? =
        null

    override fun findByCanonicalName(
        name: String
    ): FoodKnowledgeSourceEntry? =
        null

    override fun nutritionReference(
        canonicalName: String
    ): String? =
        null

    override fun carbonReference(
        canonicalName: String
    ): String? =
        null

    override fun nutritionFacts(
        canonicalName: String
    ): NutritionFacts? =
        null

    override fun carbonFootprint(
        canonicalName: String
    ): CarbonFootprint? =
        null

    override fun waterFootprint(
        canonicalName: String
    ): WaterFootprint? =
        null

    override fun waterStress(
        canonicalName: String
    ): WaterStress? =
        null

    override fun biodiversity(
        canonicalName: String
    ): BiodiversityScore? =
        null

    override fun pollinator(
        canonicalName: String
    ): PollinatorScore? =
        null

    override fun pesticide(
        canonicalName: String
    ): Pesticide? =
        null

    override fun production(
        canonicalName: String
    ): Set<ProductionMethod>? =
        null

    override fun processing(
        canonicalName: String
    ): ProcessingLevel? =
        null

    override fun packaging(
        canonicalName: String
    ): Packaging? =
        null

    override fun locality(
        canonicalName: String
    ): Locality? =
        null

    override fun foodMiles(
        canonicalName: String
    ): FoodMiles? =
        null

    override fun fairTrade(
        canonicalName: String
    ): FairTrade? =
        null

    override fun animalWelfare(
        canonicalName: String
    ): AnimalWelfare? =
        null

    override fun ingredients(
        canonicalName: String
    ): Set<String>? =
        null

    override fun allergens(
        canonicalName: String
    ): Set<Allergen>? =
        null

    override fun taxonomy(
        canonicalName: String
    ): List<String>? =
        null

    override fun seasonality(
        canonicalName: String
    ): List<Int>? =
        null

    override fun dietClassifications(
        canonicalName: String
    ): Set<DietClassification>? =
        null

    override fun nutriScore(
        canonicalName: String
    ): NutriScore? =
        null

    override fun carbonImpact(
        canonicalName: String
    ): CarbonImpactLevel? =
        null

    override fun glycemicIndex(
        canonicalName: String
    ): GlycemicIndexLevel? =
        null

    override fun ingredientGraph(
        canonicalName: String
    ): IngredientGraphEntry? =
        null

    override fun recipeGraph(
        canonicalName: String
    ): RecipeGraphEntry? =
        null

    override fun recipes(
        canonicalName: String
    ): Set<String>? =
        null
}