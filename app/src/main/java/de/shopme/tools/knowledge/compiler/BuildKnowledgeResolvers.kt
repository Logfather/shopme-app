package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.allergen.AllergenResolver
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareResolver
import de.shopme.tools.knowledge.biodiversity.BiodiversityResolver
import de.shopme.tools.knowledge.carbon.CarbonFootprintResolver
import de.shopme.tools.knowledge.diet.DietResolver
import de.shopme.tools.knowledge.fairtrade.FairTradeResolver
import de.shopme.tools.knowledge.foodmiles.FoodMilesResolver
import de.shopme.tools.knowledge.glycemic.GlycemicIndexResolver
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphResolver
import de.shopme.tools.knowledge.ingredients.IngredientsResolver
import de.shopme.tools.knowledge.locality.LocalityResolver
import de.shopme.tools.knowledge.nutriscore.NutriScoreResolver
import de.shopme.tools.knowledge.nutrition.NutritionAliasResolver
import de.shopme.tools.knowledge.nutrition.NutritionFactsResolver
import de.shopme.tools.knowledge.packaging.PackagingResolver
import de.shopme.tools.knowledge.pesticide.PesticideResolver
import de.shopme.tools.knowledge.pollinator.PollinatorResolver
import de.shopme.tools.knowledge.processing.ProcessingResolver
import de.shopme.tools.knowledge.production.ProductionResolver
import de.shopme.tools.knowledge.recipe.RecipeResolver
import de.shopme.tools.knowledge.recipegraph.RecipeGraphResolver
import de.shopme.tools.knowledge.seasonality.SeasonalityResolver
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyResolver
import de.shopme.tools.knowledge.waterfootprint.WaterResolver
import de.shopme.tools.knowledge.waterstress.WaterStressResolver

data class BuildKnowledgeResolvers(

    val aliasResolver: NutritionAliasResolver,
    val nutritionFactsResolver: NutritionFactsResolver,
    val allergenResolver: AllergenResolver,
    val glycemicIndexResolver: GlycemicIndexResolver,

    val carbonFootprintResolver: CarbonFootprintResolver,
    val waterResolver: WaterResolver,
    val waterStressResolver: WaterStressResolver,
    val biodiversityResolver: BiodiversityResolver,
    val pollinatorResolver: PollinatorResolver,
    val pesticideResolver: PesticideResolver,

    val productionResolver: ProductionResolver,
    val processingResolver: ProcessingResolver,
    val packagingResolver: PackagingResolver,
    val localityResolver: LocalityResolver,
    val foodMilesResolver: FoodMilesResolver,
    val fairTradeResolver: FairTradeResolver,
    val animalWelfareResolver: AnimalWelfareResolver,

    val ingredientsResolver: IngredientsResolver,
    val taxonomyResolver: FoodTaxonomyResolver,
    val seasonalityResolver: SeasonalityResolver,

    val dietResolver: DietResolver,
    val nutriScoreResolver: NutriScoreResolver,

    val ingredientGraphResolver: IngredientGraphResolver,
    val recipeGraphResolver: RecipeGraphResolver,
    val recipeResolver: RecipeResolver

)