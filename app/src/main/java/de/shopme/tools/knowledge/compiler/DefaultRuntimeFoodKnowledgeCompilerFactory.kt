package de.shopme.tools.knowledge.compiler

import android.content.Context
import de.shopme.tools.knowledge.allergen.AllergenKnowledge
import de.shopme.tools.knowledge.allergen.DefaultAllergenResolver
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.animalwelfare.DefaultAnimalWelfareResolver
import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledge
import de.shopme.tools.knowledge.biodiversity.DefaultBiodiversityResolver
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.carbon.DefaultCarbonFootprintResolver
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
import de.shopme.tools.knowledge.diet.DefaultDietResolver
import de.shopme.tools.knowledge.diet.DietKnowledge
import de.shopme.tools.knowledge.fairtrade.DefaultFairTradeResolver
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledge
import de.shopme.tools.knowledge.foodmiles.DefaultFoodMilesResolver
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.glycemic.DefaultGlycemicIndexResolver
import de.shopme.tools.knowledge.glycemic.GlycemicIndexKnowledge
import de.shopme.tools.knowledge.ingredientgraph.DefaultIngredientGraphResolver
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge
import de.shopme.tools.knowledge.ingredients.DefaultIngredientsResolver
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.loader.RuntimeKnowledgeLoader
import de.shopme.tools.knowledge.locality.DefaultLocalityResolver
import de.shopme.tools.knowledge.locality.LocalityKnowledge
import de.shopme.tools.knowledge.nutriscore.DefaultNutriScoreResolver
import de.shopme.tools.knowledge.nutriscore.NutriScoreFactsKnowledge
import de.shopme.tools.knowledge.nutrition.DefaultNutritionAliasResolver
import de.shopme.tools.knowledge.nutrition.DefaultNutritionFactsResolver
import de.shopme.tools.knowledge.nutrition.NutritionAliasGraphLoader
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import de.shopme.tools.knowledge.packaging.DefaultPackagingResolver
import de.shopme.tools.knowledge.packaging.PackagingKnowledge
import de.shopme.tools.knowledge.pesticide.DefaultPesticideResolver
import de.shopme.tools.knowledge.pesticide.PesticideKnowledge
import de.shopme.tools.knowledge.pollinator.DefaultPollinatorResolver
import de.shopme.tools.knowledge.pollinator.PollinatorKnowledge
import de.shopme.tools.knowledge.processing.DefaultProcessingResolver
import de.shopme.tools.knowledge.processing.ProcessingKnowledge
import de.shopme.tools.knowledge.production.DefaultProductionResolver
import de.shopme.tools.knowledge.production.ProductionKnowledge
import de.shopme.tools.knowledge.recipe.DefaultRecipeResolver
import de.shopme.tools.knowledge.recipe.RecipeKnowledge
import de.shopme.tools.knowledge.recipegraph.DefaultRecipeGraphResolver
import de.shopme.tools.knowledge.recipegraph.RecipeGraphKnowledge
import de.shopme.tools.knowledge.seasonality.DefaultSeasonalityResolver
import de.shopme.tools.knowledge.seasonality.SeasonalityKnowledge
import de.shopme.tools.knowledge.taxonomy.DefaultFoodTaxonomyResolver
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyKnowledge
import de.shopme.tools.knowledge.waterfootprint.DefaultWaterResolver
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledge
import de.shopme.tools.knowledge.waterstress.DefaultWaterStressResolver
import de.shopme.tools.knowledge.waterstress.WaterStressKnowledge

object DefaultRuntimeFoodKnowledgeCompilerFactory {

    fun create(
        context: Context
    ): FoodKnowledgeCompiler {

        val aliasResolver =
            DefaultNutritionAliasResolver(
                NutritionAliasGraphLoader(
                    context
                ).load()
            )

        val nutritionFactsResolver =
            DefaultNutritionFactsResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "nutrition.json",
                    NutritionFactsKnowledge::class.java
                ).load()
            )

        val allergenResolver =
            DefaultAllergenResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "allergens.json",
                    AllergenKnowledge::class.java
                ).load()
            )

        val glycemicIndexResolver =
            DefaultGlycemicIndexResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "glycemic.json",
                    GlycemicIndexKnowledge::class.java
                ).load()
            )

        val carbonFootprintResolver =
            DefaultCarbonFootprintResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "carbon_footprint.json",
                    CarbonKnowledge::class.java
                ).load()
            )

        val seasonalityResolver =
            DefaultSeasonalityResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "seasonality.json",
                    SeasonalityKnowledge::class.java
                ).load()
            )

        val processingResolver =
            DefaultProcessingResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "processing.json",
                    ProcessingKnowledge::class.java
                ).load()
            )

        val productionResolver =
            DefaultProductionResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "production.json",
                    ProductionKnowledge::class.java
                ).load()
            )

        val packagingResolver =
            DefaultPackagingResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "packaging.json",
                    PackagingKnowledge::class.java
                ).load()
            )

        val localityResolver =
            DefaultLocalityResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "locality.json",
                    LocalityKnowledge::class.java
                ).load()
            )

        val foodMilesResolver =
            DefaultFoodMilesResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "food_miles.json",
                    FoodMilesKnowledge::class.java
                ).load()
            )

        val taxonomyResolver =
            DefaultFoodTaxonomyResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "food_taxonomy.json",
                    FoodTaxonomyKnowledge::class.java
                ).load()
            )

        val waterResolver =
            DefaultWaterResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "water_footprint.json",
                    WaterKnowledge::class.java
                ).load()
            )

        val waterStressResolver =
            DefaultWaterStressResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "water_stress.json",
                    WaterStressKnowledge::class.java
                ).load()
            )

        val biodiversityResolver =
            DefaultBiodiversityResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "biodiversity.json",
                    BiodiversityKnowledge::class.java
                ).load()
            )

        val pollinatorResolver =
            DefaultPollinatorResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "pollinator.json",
                    PollinatorKnowledge::class.java
                ).load()
            )

        val pesticideResolver =
            DefaultPesticideResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "pesticides.json",
                    PesticideKnowledge::class.java
                ).load()
            )

        val fairTradeResolver =
            DefaultFairTradeResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "fairtrade.json",
                    FairTradeKnowledge::class.java
                ).load()
            )

        val animalWelfareResolver =
            DefaultAnimalWelfareResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "animal_welfare.json",
                    AnimalWelfareKnowledge::class.java
                ).load()
            )

        val dietResolver =
            DefaultDietResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "diet_classification.json",
                    DietKnowledge::class.java
                ).load()
            )

        val nutriScoreResolver =
            DefaultNutriScoreResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "nutri_score.json",
                    NutriScoreFactsKnowledge::class.java
                ).load()
            )

        val ingredientsResolver =
            DefaultIngredientsResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "ingredients.json",
                    IngredientsKnowledge::class.java
                ).load()
            )

        val ingredientGraphResolver =
            DefaultIngredientGraphResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "ingredient_graph.json",
                    IngredientGraphKnowledge::class.java
                ).load()
            )

        val recipeGraphResolver =
            DefaultRecipeGraphResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "recipe_graph.json",
                    RecipeGraphKnowledge::class.java
                ).load()
            )

        val recipeResolver =
            DefaultRecipeResolver(
                RuntimeKnowledgeLoader(
                    context,
                    "recipes.json",
                    RecipeKnowledge::class.java
                ).load()
            )

        return DefaultFoodKnowledgeBuildCompiler.create(

            FoodKnowledgeCompilerPipeline(

                listOf(

                    NutritionAliasCompilerPass(
                        resolver = aliasResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    NutritionCompilerPass(
                        resolver = nutritionFactsResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    AllergenCompilerPass(
                        resolver = allergenResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    GlycemicIndexCompilerPass(
                        resolver = glycemicIndexResolver,
                        foodLookup = EmptyFoodLookup),

                    CarbonCompilerPass(carbonFootprintResolver,
                        foodLookup = EmptyFoodLookup),

                    WaterCompilerPass(
                        resolver = waterResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    WaterStressCompilerPass(
                        resolver = waterStressResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    BiodiversityCompilerPass(
                        resolver = biodiversityResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    PollinatorCompilerPass(
                        resolver = pollinatorResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    PesticideCompilerPass(
                        resolver = pesticideResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    FairTradeCompilerPass(
                        resolver = fairTradeResolver,
                        foodLookup = EmptyFoodLookup),
                    AnimalWelfareCompilerPass(
                        resolver = animalWelfareResolver,
                        foodLookup = EmptyFoodLookup),

                    ProductionCompilerPass(
                        resolver = productionResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    PackagingCompilerPass(
                        resolver = packagingResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    ProcessingCompilerPass(
                        resolver = processingResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    LocalityCompilerPass(
                        resolver = localityResolver,
                        foodLookup = EmptyFoodLookup),
                    FoodMilesCompilerPass(
                        resolver = foodMilesResolver,
                        foodLookup = EmptyFoodLookup),
                    TaxonomyCompilerPass(
                        resolver = taxonomyResolver,
                        foodLookup = EmptyFoodLookup
                    ),
                    SeasonalityCompilerPass(
                        resolver = seasonalityResolver,
                        foodLookup = EmptyFoodLookup
                    ),

                    CarbonImpactCompilerPass(
                        foodLookup = EmptyFoodLookup
                    ),
                    DietCompilerPass(
                        resolver = dietResolver,
                        foodLookup = EmptyFoodLookup),
                    NutriScoreCompilerPass(
                        resolver = nutriScoreResolver,
                        foodLookup = EmptyFoodLookup),

                    IngredientsCompilerPass(
                        resolver = ingredientsResolver,
                        foodLookup = EmptyFoodLookup),
                    IngredientGraphCompilerPass(
                        resolver = ingredientGraphResolver,
                        foodLookup = EmptyFoodLookup),
                    RecipeGraphCompilerPass(
                        resolver = recipeGraphResolver,
                        foodLookup = EmptyFoodLookup),
                    RecipeCompilerPass(
                        resolver = recipeResolver,
                        foodLookup = EmptyFoodLookup)

                )

            )

        )
    }
}