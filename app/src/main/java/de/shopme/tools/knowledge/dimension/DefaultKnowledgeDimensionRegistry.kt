package de.shopme.tools.knowledge.dimension

import de.shopme.tools.knowledge.dimension.capabilities.AnimalWelfareCapability
import de.shopme.tools.knowledge.dimension.capabilities.BiodiversityCapability
import de.shopme.tools.knowledge.dimension.capabilities.CarbonCapability
import de.shopme.tools.knowledge.dimension.capabilities.DietCapability
import de.shopme.tools.knowledge.dimension.capabilities.FairTradeCapability
import de.shopme.tools.knowledge.dimension.capabilities.FoodMilesCapability
import de.shopme.tools.knowledge.dimension.capabilities.FoodTaxonomyCapability
import de.shopme.tools.knowledge.dimension.capabilities.GlycemicCapability
import de.shopme.tools.knowledge.dimension.capabilities.IngredientGraphCapability
import de.shopme.tools.knowledge.dimension.capabilities.IngredientsCapability
import de.shopme.tools.knowledge.dimension.capabilities.LocalityCapability
import de.shopme.tools.knowledge.dimension.capabilities.NutriScoreCapability
import de.shopme.tools.knowledge.dimension.capabilities.PackagingCapability
import de.shopme.tools.knowledge.dimension.capabilities.PesticideCapability
import de.shopme.tools.knowledge.dimension.capabilities.PollinatorCapability
import de.shopme.tools.knowledge.dimension.capabilities.ProcessingCapability
import de.shopme.tools.knowledge.dimension.capabilities.ProductionCapability
import de.shopme.tools.knowledge.dimension.capabilities.RecipeCapability
import de.shopme.tools.knowledge.dimension.capabilities.SeasonalityCapability
import de.shopme.tools.knowledge.dimension.capabilities.WaterCapability
import de.shopme.tools.knowledge.dimension.capabilities.WaterStressCapability

object DefaultKnowledgeDimensionRegistry {

    fun create() =

        KnowledgeDimensionRegistry(

            listOf(
                NutritionCapability(),

                CarbonCapability(),

                WaterCapability(),

                WaterStressCapability(),

                BiodiversityCapability(),

                PollinatorCapability(),

                ProcessingCapability(),

                FairTradeCapability(),

                PackagingCapability(),

                ProductionCapability(),

                SeasonalityCapability(),

                LocalityCapability(),

                FoodMilesCapability(),

                AnimalWelfareCapability(),

                PesticideCapability(),

                GlycemicCapability(),

                DietCapability(),

                NutriScoreCapability(),

                FoodTaxonomyCapability(),

                IngredientsCapability(),

                IngredientGraphCapability(),

                RecipeCapability()

            )

        )

}