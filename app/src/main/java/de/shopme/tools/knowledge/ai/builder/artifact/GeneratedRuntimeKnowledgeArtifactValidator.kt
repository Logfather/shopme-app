package de.shopme.tools.knowledge.ai.builder.artifact

import de.shopme.tools.knowledge.allergen.AllergenKnowledge
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledge
import de.shopme.tools.knowledge.diet.DietKnowledge
import de.shopme.tools.knowledge.environment.EnvironmentalImpactKnowledge
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledge
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.locality.LocalityKnowledge
import de.shopme.tools.knowledge.nutriscore.NutriScoreFactsKnowledge
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import de.shopme.tools.knowledge.packaging.PackagingKnowledge
import de.shopme.tools.knowledge.pesticides.PesticideKnowledge
import de.shopme.tools.knowledge.pollinator.PollinatorKnowledge
import de.shopme.tools.knowledge.processing.ProcessingKnowledge
import de.shopme.tools.knowledge.production.ProductionKnowledge
import de.shopme.tools.knowledge.recipe.RecipeKnowledge
import de.shopme.tools.knowledge.recipegraph.RecipeGraphKnowledge
import de.shopme.tools.knowledge.seasonality.SeasonalityKnowledge
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyKnowledge
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledge
import de.shopme.tools.knowledge.waterstress.WaterStressKnowledge

class GeneratedRuntimeKnowledgeArtifactValidator {

    fun validate(
        artifactName: String,
        artifact: Any
    ) {
        try {
            when (artifact) {
                is NutritionFactsKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is EnvironmentalImpactKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )
                is IngredientsKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is AllergenKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is PackagingKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is FoodTaxonomyKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is ProcessingKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is WaterKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is WaterStressKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is BiodiversityKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is PollinatorKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is PesticideKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is ProductionKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is FoodMilesKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is LocalityKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is NutriScoreFactsKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is SeasonalityKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is DietKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is FairTradeKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is AnimalWelfareKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is RecipeKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is IngredientGraphKnowledge ->
                    validateEntries(
                        artifactName = artifactName,
                        entries = artifact.entries
                    )

                is RecipeGraphKnowledge ->
                    validateRecipeGraphKnowledge(
                        fileName = artifactName,
                        knowledge = artifact
                    )

                else ->
                    error(
                        "Unsupported runtime knowledge artifact: " +
                                "$artifactName (${artifact::class.qualifiedName})"
                    )
            }
        } catch (error: IllegalArgumentException) {
            println()
            println("RUNTIME KNOWLEDGE VALIDATION FAILED")
            println("-----------------------------------")
            println("Artifact: $artifactName")
            println("Reason: ${error.message}")
            println()

            throw error
        }
    }

    private fun validateRecipeGraphKnowledge(
        fileName: String,
        knowledge: RecipeGraphKnowledge
    ) {
        require(knowledge.entries.isNotEmpty()) {
            "Runtime artifact must not be empty: $fileName"
        }

        require(
            knowledge.entries.keys.all { it.trim() == it && it.isNotBlank() }
        ) {
            "Runtime artifact contains invalid keys: $fileName"
        }
    }

    private fun validateEntries(
        artifactName: String,
        entries: Map<String, Any>
    ) {
        require(entries.isNotEmpty()) {
            "Runtime knowledge artifact must not be empty"
        }

        entries.keys.forEach { key ->

            require(key.isNotBlank()) {
                "Contains blank key"
            }

            require(key == key.trim()) {
                "Contains untrimmed key='$key'"
            }

            require(key.lowercase() == key) {
                "Contains non-normalized key='$key'"
            }
        }

        val keys =
            entries.keys.toList()

        require(keys == keys.sorted()) {

            val sortedKeys =
                keys.sorted()

            val firstMismatch =
                keys.zip(sortedKeys)
                    .firstOrNull {
                        it.first != it.second
                    }

            """
    Runtime knowledge artifact keys are not sorted: $artifactName
    
    first mismatch:
    actual=${firstMismatch?.first}
    expected=${firstMismatch?.second}
    
    firstActual=${keys.take(10)}
    firstExpected=${sortedKeys.take(10)}
    """.trimIndent()
        }
    }
}