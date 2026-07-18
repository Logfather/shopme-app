package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.allergen.AllergenKnowledge
import de.shopme.tools.knowledge.allergen.DefaultAllergenResolver
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.animalwelfare.DefaultAnimalWelfareResolver
import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledge
import de.shopme.tools.knowledge.biodiversity.DefaultBiodiversityResolver
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.carbon.DefaultCarbonFootprintResolver
import de.shopme.tools.knowledge.diet.DefaultDietResolver
import de.shopme.tools.knowledge.diet.DietKnowledge
import de.shopme.tools.knowledge.fairtrade.DefaultFairTradeResolver
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledge
import de.shopme.tools.knowledge.foodmiles.DefaultFoodMilesResolver
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.glycemic.DefaultGlycemicIndexResolver
import de.shopme.tools.knowledge.glycemic.GlycemicIndexKnowledge
import de.shopme.tools.knowledge.ingredientgraph.DefaultIngredientGraphResolver
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge
import de.shopme.tools.knowledge.ingredients.DefaultIngredientsResolver
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.locality.DefaultLocalityResolver
import de.shopme.tools.knowledge.locality.LocalityKnowledge
import de.shopme.tools.knowledge.nutriscore.DefaultNutriScoreResolver
import de.shopme.tools.knowledge.nutriscore.NutriScoreFactsKnowledge
import de.shopme.tools.knowledge.nutrition.DefaultNutritionAliasResolver
import de.shopme.tools.knowledge.nutrition.DefaultNutritionFactsResolver
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import de.shopme.tools.knowledge.packaging.DefaultPackagingResolver
import de.shopme.tools.knowledge.packaging.PackagingKnowledge
import de.shopme.tools.knowledge.pesticides.DefaultPesticideResolver
import de.shopme.tools.knowledge.pesticides.PesticideKnowledge
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
import java.io.File

class BuildKnowledgeResolversFactory(

    private val runtimeDirectory: File

) {

    private val gson =
        Gson()

    fun create(): BuildKnowledgeResolvers {

        return BuildKnowledgeResolvers(

            aliasResolver =
                DefaultNutritionAliasResolver(
                    loadNutritionAliases()
                ),

            nutritionFactsResolver =
                DefaultNutritionFactsResolver(
                    load("nutrition.json", NutritionFactsKnowledge::class.java)
                ),

            allergenResolver =
                DefaultAllergenResolver(
                    load("allergens.json", AllergenKnowledge::class.java)
                ),

            glycemicIndexResolver =
                DefaultGlycemicIndexResolver(
                    load("glycemic.json", GlycemicIndexKnowledge::class.java)
                ),

            carbonFootprintResolver =
                DefaultCarbonFootprintResolver(
                    load("carbon_footprint.json", CarbonKnowledge::class.java)
                ),

            waterResolver =
                DefaultWaterResolver(
                    load("water_footprint.json", WaterKnowledge::class.java)
                ),

            waterStressResolver =
                DefaultWaterStressResolver(
                    load("water_stress.json", WaterStressKnowledge::class.java)
                ),

            biodiversityResolver =
                DefaultBiodiversityResolver(
                    load("biodiversity.json", BiodiversityKnowledge::class.java)
                ),

            pollinatorResolver =
                DefaultPollinatorResolver(
                    load("pollinator.json", PollinatorKnowledge::class.java)
                ),

            pesticideResolver =
                DefaultPesticideResolver(
                    load("pesticides.json", PesticideKnowledge::class.java)
                ),

            productionResolver =
                DefaultProductionResolver(
                    load("production.json", ProductionKnowledge::class.java)
                ),

            processingResolver =
                DefaultProcessingResolver(
                    load("processing.json", ProcessingKnowledge::class.java)
                ),

            packagingResolver =
                DefaultPackagingResolver(
                    load("packaging.json", PackagingKnowledge::class.java)
                ),

            localityResolver =
                DefaultLocalityResolver(
                    load("locality.json", LocalityKnowledge::class.java)
                ),

            foodMilesResolver =
                DefaultFoodMilesResolver(
                    load("food_miles.json", FoodMilesKnowledge::class.java)
                ),

            fairTradeResolver =
                DefaultFairTradeResolver(
                    load("fairtrade.json", FairTradeKnowledge::class.java)
                ),

            animalWelfareResolver =
                DefaultAnimalWelfareResolver(
                    load("animal_welfare.json", AnimalWelfareKnowledge::class.java)
                ),

            ingredientsResolver =
                DefaultIngredientsResolver(
                    load("ingredients.json", IngredientsKnowledge::class.java)
                ),

            taxonomyResolver =
                DefaultFoodTaxonomyResolver(
                    load("food_taxonomy.json", FoodTaxonomyKnowledge::class.java)
                ),

            seasonalityResolver =
                DefaultSeasonalityResolver(
                    load("seasonality.json", SeasonalityKnowledge::class.java)
                ),

            dietResolver =
                DefaultDietResolver(
                    load("diet_classification.json", DietKnowledge::class.java)
                ),

            nutriScoreResolver =
                DefaultNutriScoreResolver(
                    load("nutri_score.json", NutriScoreFactsKnowledge::class.java)
                ),

            ingredientGraphResolver =
                DefaultIngredientGraphResolver(
                    load("ingredient_graph.json", IngredientGraphKnowledge::class.java)
                ),

            recipeGraphResolver =
                DefaultRecipeGraphResolver(
                    load("recipe_graph.json", RecipeGraphKnowledge::class.java)
                ),

            recipeResolver =
                DefaultRecipeResolver(
                    load("recipes.json", RecipeKnowledge::class.java)
                )
        )
    }

    private fun <T> load(
        fileName: String,
        clazz: Class<T>
    ): T {

        val file =
            File(
                runtimeDirectory,
                fileName
            )

        return gson.fromJson(
            file.readText(),
            clazz
        )
    }

    private fun loadNutritionAliases(): Map<String, String> {

        val file =
            File(
                runtimeDirectory,
                "nutrition_alias.json"
            )

        val type =
            object : TypeToken<Map<String, String>>() {}.type

        return gson.fromJson(
            file.readText(),
            type
        )
    }
}