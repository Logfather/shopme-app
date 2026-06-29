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
import de.shopme.tools.knowledge.pesticide.Pesticide
import de.shopme.tools.knowledge.pollinator.PollinatorScore
import de.shopme.tools.knowledge.processing.ProcessingLevel
import de.shopme.tools.knowledge.production.ProductionMethod
import de.shopme.tools.knowledge.recipegraph.RecipeGraphEntry
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterstress.WaterStress

interface FoodLookup {

    fun findById(
        id: String
    ): FoodKnowledgeSourceEntry?

    fun findByCanonicalName(
        name: String
    ): FoodKnowledgeSourceEntry?

    fun nutritionReference(
        canonicalName: String
    ): String?

    fun carbonReference(
        canonicalName: String
    ): String?

    fun nutritionFacts(
        canonicalName: String
    ): NutritionFacts?

    fun carbonFootprint(
        canonicalName: String
    ): CarbonFootprint?

    fun waterFootprint(
        canonicalName: String
    ): WaterFootprint?

    fun waterStress(
        canonicalName: String
    ): WaterStress?

    fun biodiversity(
        canonicalName: String
    ): BiodiversityScore?

    fun pollinator(
        canonicalName: String
    ): PollinatorScore?

    fun pesticide(
        canonicalName: String
    ): Pesticide?

    fun production(
        canonicalName: String
    ): Set<ProductionMethod>?

    fun processing(
        canonicalName: String
    ): ProcessingLevel?

    fun packaging(
        canonicalName: String
    ): Packaging?

    fun locality(
        canonicalName: String
    ): Locality?

    fun fairTrade(
        canonicalName: String
    ): FairTrade?

    fun foodMiles(
        canonicalName: String
    ): FoodMiles?

    fun animalWelfare(
        canonicalName: String
    ): AnimalWelfare?

    fun ingredients(
        canonicalName: String
    ): Set<String>?

    fun allergens(
        canonicalName: String
    ): Set<Allergen>?

    fun taxonomy(
        canonicalName: String
    ): List<String>?

    fun seasonality(
        canonicalName: String
    ): List<Int>?

    fun dietClassifications(
        canonicalName: String
    ): Set<DietClassification>?

    fun nutriScore(
        canonicalName: String
    ): NutriScore?

    fun carbonImpact(
        canonicalName: String
    ): CarbonImpactLevel?

    fun glycemicIndex(
        canonicalName: String
    ): GlycemicIndexLevel?

    fun ingredientGraph(
        canonicalName: String
    ): IngredientGraphEntry?

    fun recipeGraph(
        canonicalName: String
    ): RecipeGraphEntry?

    fun recipes(
        canonicalName: String
    ): Set<String>?
}