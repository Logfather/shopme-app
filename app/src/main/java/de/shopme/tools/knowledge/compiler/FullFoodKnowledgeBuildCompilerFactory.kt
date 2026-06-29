package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.passes.AllergenCompilerPass
import de.shopme.tools.knowledge.compiler.passes.AnimalWelfareCompilerPass
import de.shopme.tools.knowledge.compiler.passes.BiodiversityCompilerPass
import de.shopme.tools.knowledge.compiler.passes.CarbonCompilerPass
import de.shopme.tools.knowledge.compiler.passes.CarbonImpactCompilerPass
import de.shopme.tools.knowledge.compiler.passes.DietCompilerPass
import de.shopme.tools.knowledge.compiler.passes.FairTradeCompilerPass
import de.shopme.tools.knowledge.compiler.passes.FoodMilesCompilerPass
import de.shopme.tools.knowledge.compiler.passes.GlycemicIndexCompilerPass
import de.shopme.tools.knowledge.compiler.passes.IngredientGraphCompilerPass
import de.shopme.tools.knowledge.compiler.passes.IngredientsCompilerPass
import de.shopme.tools.knowledge.compiler.passes.LocalityCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutriScoreCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionCompilerPass
import de.shopme.tools.knowledge.compiler.passes.PackagingCompilerPass
import de.shopme.tools.knowledge.compiler.passes.PesticideCompilerPass
import de.shopme.tools.knowledge.compiler.passes.PollinatorCompilerPass
import de.shopme.tools.knowledge.compiler.passes.ProcessingCompilerPass
import de.shopme.tools.knowledge.compiler.passes.ProductionCompilerPass
import de.shopme.tools.knowledge.compiler.passes.RecipeCompilerPass
import de.shopme.tools.knowledge.compiler.passes.RecipeGraphCompilerPass
import de.shopme.tools.knowledge.compiler.passes.SeasonalityCompilerPass
import de.shopme.tools.knowledge.compiler.passes.TaxonomyCompilerPass
import de.shopme.tools.knowledge.compiler.passes.WaterCompilerPass
import de.shopme.tools.knowledge.compiler.passes.WaterStressCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

object FullFoodKnowledgeBuildCompilerFactory {

    fun create(
        foodLookup: FoodLookup,
        resolvers: BuildKnowledgeResolvers
    ): FoodKnowledgeCompiler {
        return DefaultFoodKnowledgeBuildCompiler.create(
            FoodKnowledgeCompilerPipeline(
                listOf(
                    NutritionAliasCompilerPass(resolvers.aliasResolver, foodLookup),
                    NutritionCompilerPass(resolvers.nutritionFactsResolver, foodLookup),
                    AllergenCompilerPass(resolvers.allergenResolver, foodLookup),
                    GlycemicIndexCompilerPass(resolvers.glycemicIndexResolver, foodLookup),

                    CarbonCompilerPass(resolvers.carbonFootprintResolver, foodLookup),
                    WaterCompilerPass(resolvers.waterResolver, foodLookup),
                    WaterStressCompilerPass(resolvers.waterStressResolver, foodLookup),
                    BiodiversityCompilerPass(resolvers.biodiversityResolver, foodLookup),
                    PollinatorCompilerPass(resolvers.pollinatorResolver, foodLookup),
                    PesticideCompilerPass(resolvers.pesticideResolver, foodLookup),

                    ProductionCompilerPass(resolvers.productionResolver, foodLookup),
                    PackagingCompilerPass(resolvers.packagingResolver, foodLookup),
                    ProcessingCompilerPass(resolvers.processingResolver, foodLookup),
                    LocalityCompilerPass(resolvers.localityResolver, foodLookup),
                    FoodMilesCompilerPass(resolvers.foodMilesResolver, foodLookup),
                    FairTradeCompilerPass(resolvers.fairTradeResolver, foodLookup),
                    AnimalWelfareCompilerPass(resolvers.animalWelfareResolver, foodLookup),

                    TaxonomyCompilerPass(resolvers.taxonomyResolver, foodLookup),
                    SeasonalityCompilerPass(resolvers.seasonalityResolver, foodLookup),

                    CarbonImpactCompilerPass(foodLookup),
                    DietCompilerPass(resolvers.dietResolver, foodLookup),
                    NutriScoreCompilerPass(resolvers.nutriScoreResolver, foodLookup),

                    IngredientsCompilerPass(resolvers.ingredientsResolver, foodLookup),
                    IngredientGraphCompilerPass(resolvers.ingredientGraphResolver, foodLookup),
                    RecipeGraphCompilerPass(resolvers.recipeGraphResolver, foodLookup),
                    RecipeCompilerPass(resolvers.recipeResolver, foodLookup)
                )
            )
        )
    }
}