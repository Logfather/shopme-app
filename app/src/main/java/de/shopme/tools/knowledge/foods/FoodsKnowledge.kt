package de.shopme.tools.knowledge.foods

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.KnowledgeArtifact
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

data class FoodsKnowledge(
    val version: Int,
    val foods: List<FoodKnowledgeSourceEntry>
) : KnowledgeArtifact

data class FoodKnowledgeSourceEntry(
    val id: String,
    val names: FoodNames,
    val knowledge: FoodKnowledgeSources = FoodKnowledgeSources()
)

data class FoodNames(
    val canonical: String,
    val aliases: List<String> = emptyList()
)

data class FoodKnowledgeSource<T>(
    val reference: String? = null,
    val source: String? = null,
    val value: T? = null
)

data class FoodKnowledgeSources(
    val nutrition: FoodKnowledgeSource<NutritionFacts>? = null,
    val carbon: FoodKnowledgeSource<CarbonFootprint>? = null,
    val water: FoodKnowledgeSource<WaterFootprint>? = null,
    val waterStress: FoodKnowledgeSource<WaterStress>? = null,
    val biodiversity: FoodKnowledgeSource<BiodiversityScore>? = null,
    val pollinator: FoodKnowledgeSource<PollinatorScore>? = null,
    val pesticide: FoodKnowledgeSource<Pesticide>? = null,
    val production: FoodKnowledgeSource<Set<ProductionMethod>>? = null,
    val processing: FoodKnowledgeSource<ProcessingLevel>? = null,
    val packaging: FoodKnowledgeSource<Packaging>? = null,
    val locality: FoodKnowledgeSource<Locality>? = null,
    val foodMiles: FoodKnowledgeSource<FoodMiles>? = null,
    val fairTrade: FoodKnowledgeSource<FairTrade>? = null,
    val animalWelfare: FoodKnowledgeSource<AnimalWelfare>? = null,
    val ingredients: FoodKnowledgeSource<Set<String>>? = null,
    val allergens: FoodKnowledgeSource<Set<Allergen>>? = null,
    val taxonomy: FoodKnowledgeSource<List<String>>? = null,
    val seasonality: FoodKnowledgeSource<List<Int>>? = null,
    val dietClassifications: FoodKnowledgeSource<Set<DietClassification>>? = null,
    val nutriScore: FoodKnowledgeSource<NutriScore>? = null,
    val carbonImpact: FoodKnowledgeSource<CarbonImpactLevel>? = null,
    val glycemicIndex: FoodKnowledgeSource<GlycemicIndexLevel>? = null,
    val ingredientGraph: FoodKnowledgeSource<IngredientGraphEntry>? = null,
    val recipeGraph: FoodKnowledgeSource<RecipeGraphEntry>? = null,
    val recipes: FoodKnowledgeSource<Set<String>>? = null
)