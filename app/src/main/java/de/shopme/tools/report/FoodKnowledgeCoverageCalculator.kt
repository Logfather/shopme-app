package de.shopme.tools.report

import android.util.Log
import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.domain.food.GlycemicIndexLevel

class FoodKnowledgeCoverageCalculator {


    private fun countCoverage(

        knowledge: List<FoodKnowledgeEntry>,

        hasKnowledge: (FoodKnowledgeEntry) -> Boolean

    ): Int {

        return knowledge.count {

            hasKnowledge(

                it

            )

        }

    }

    fun calculate(

        knowledge: List<FoodKnowledgeEntry>

    ): List<FoodKnowledgeCoverageEntry> {

        val total = knowledge.size

        Log.d(

            "HIVRA_COVERAGE",

            "knowledge=${knowledge.size}"

        )

        knowledge.firstOrNull()?.let {

            Log.d(

                "HIVRA_COVERAGE",

                it.toString()

            )

        }

        return listOf(

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.ALLERGEN.displayName,

                covered = allergenCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.ANIMALWELFARE.displayName,

                covered = animalwelfareCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.BIODIVERSITY.displayName,

                covered = biodiversityCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.CARBON.displayName,

                covered = carbonCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.CARBONIMPACT.displayName,

                covered = carbonImpactCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.FAIRTRADE.displayName,

                covered = fairtradeCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.FOODMILES.displayName,

                covered = foodMilesCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.GLYCEMIC.displayName,

                covered = glycemicCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.INGREDIENT.displayName,

                covered = ingredientCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.LOCALITY.displayName,

                covered = localityCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.NUTRITION.displayName,

                covered = nutritionCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.NUTRISCORE.displayName,

                covered = nutriScoreCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.PACKAGING.displayName,

                covered = packagingCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.PESTICIDE.displayName,

                covered = pesticideCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.POLLINATOR.displayName,

                covered = pollinatorCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.PROCESSING.displayName,

                covered = processingCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.RECIPE.displayName,

                covered = recipeCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.TAXONOMY.displayName,

                covered = foodTaxonomyCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.WATER.displayName,

                covered = waterCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.WATERSTRESS.displayName,

                covered = waterStressCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.DIET.displayName,

                covered = dietCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.PRODUCTION.displayName,

                covered = productionCoverage(knowledge),

                total = total

            ),

            FoodKnowledgeCoverageEntry(

                name = CoverageDimension.SEASONALITY.displayName,

                covered = seasonalityCoverage(knowledge),

                total = total

            ),

        )

    }

    private fun allergenCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.allergens.isNotEmpty()

        }

    private fun animalwelfareCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.animalWelfare != null

        }

    private fun biodiversityCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.biodiversity != null

        }

    private fun carbonCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.carbonFootprint != null

        }

    private fun fairtradeCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.fairTrade != null

        }

    private fun glycemicCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.glycemicIndex != GlycemicIndexLevel.UNKNOWN

        }

    private fun localityCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.locality != null

        }

    private fun nutritionCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.nutritionFacts != null

        }

    private fun packagingCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.packaging != null

        }

    private fun pesticideCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.pesticide != null

        }

    private fun pollinatorCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.pollinator != null

        }

    private fun processingCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.processing != null

        }

    private fun waterCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.waterFootprint != null

        }

    private fun nutriScoreCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.nutriScore != null

        }

    private fun carbonImpactCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.carbonImpact!= null

        }

    private fun foodMilesCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.foodMiles != null

        }

    private fun waterStressCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.waterStress != null

        }

    private fun foodTaxonomyCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.taxonomyPath.isNotEmpty()

        }

    private fun ingredientCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.ingredients.isNotEmpty()

        }

    private fun recipeCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.recipes.isNotEmpty()

        }

    private fun dietCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.dietClassifications.isNotEmpty()

        }

    private fun productionCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.production.isNotEmpty()

        }

    private fun seasonalityCoverage(

        knowledge: List<FoodKnowledgeEntry>

    ): Int =

        countCoverage(

            knowledge

        ) {

            it.seasonality.isNotEmpty()

        }

}