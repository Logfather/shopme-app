package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.artifacts.ExistingFoodsKnowledgeLoader
import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifactBuildSummaryPrinter
import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifactComparator
import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifactComparisonPrinter
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidateValidator
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.pipeline.FoodsKnowledgeArtifactGenerator
import de.shopme.tools.knowledge.pipeline.KnowledgeCandidateBuildPipeline
import de.shopme.tools.knowledge.source.KnowledgeSourceAdapter
import java.io.File

fun generateFoodsKnowledgeArtifactFromRuntimeFoods() {

    val existingFoodsInput =
        File("src/main/assets/knowledge/runtime/foods.json")

    check(existingFoodsInput.exists()) {
        "foods.json not found: ${existingFoodsInput.absolutePath}"
    }

    val existingFoods =
        ExistingFoodsKnowledgeLoader()
            .load(
                input = existingFoodsInput
            )

    fun mapRuntimeDimension(
        dimension: String
    ): KnowledgeDimensionCandidateType? {

        return when (dimension) {
            "nutrition", "nutritionFacts" -> KnowledgeDimensionCandidateType.NUTRITION
            "carbonImpact", "carbonFootprint" -> KnowledgeDimensionCandidateType.CARBON
            "waterFootprint" -> KnowledgeDimensionCandidateType.WATER
            "waterStress" -> KnowledgeDimensionCandidateType.WATER_STRESS
            "glycemicIndex" -> KnowledgeDimensionCandidateType.GLYCEMIC
            "allergens" -> KnowledgeDimensionCandidateType.ALLERGENS
            "ingredients" -> KnowledgeDimensionCandidateType.INGREDIENTS
            "taxonomy", "foodTaxonomy" -> KnowledgeDimensionCandidateType.TAXONOMY
            "seasonality" -> KnowledgeDimensionCandidateType.SEASONALITY
            "packaging" -> KnowledgeDimensionCandidateType.PACKAGING
            "fairTrade", "fairtrade" -> KnowledgeDimensionCandidateType.FAIRTRADE
            "animalWelfare" -> KnowledgeDimensionCandidateType.ANIMAL_WELFARE
            "biodiversity" -> KnowledgeDimensionCandidateType.BIODIVERSITY
            "pollinator" -> KnowledgeDimensionCandidateType.POLLINATOR
            "locality" -> KnowledgeDimensionCandidateType.LOCALITY
            "foodMiles" -> KnowledgeDimensionCandidateType.FOOD_MILES
            "production", "processing" -> KnowledgeDimensionCandidateType.PRODUCTION
            "recipe", "recipes" -> KnowledgeDimensionCandidateType.RECIPE
            "ingredientGraph" -> KnowledgeDimensionCandidateType.INGREDIENT_GRAPH
            "recipeGraph" -> KnowledgeDimensionCandidateType.RECIPE_GRAPH
            else -> null
        }
    }

    val sourceAdapter =
        object : KnowledgeSourceAdapter {

            override fun load(): List<CanonicalKnowledgeCandidate> {

                return existingFoods.map { food ->

                    CanonicalKnowledgeCandidate(
                        canonicalId = food.normalizedName,
                        aliases = setOf(food.normalizedName),
                        dimensions = food.knowledgeDimensions
                            .mapNotNull(::mapRuntimeDimension)
                            .distinct()
                            .map { dimension ->
                                KnowledgeDimensionCandidate(
                                    dimension = dimension,
                                    payload = food.normalizedName
                                )
                            },
                        metadata = CandidateMetadata(
                            source = "runtime_foods",
                            sourceId = food.normalizedName,
                            confidence = 1.0,
                            version = "runtime"
                        )
                    )
                }
            }
        }



    val pipeline =
        KnowledgeCandidateBuildPipeline(
            sourceAdapters = listOf(sourceAdapter),
            validator = CanonicalKnowledgeCandidateValidator()
        )

    val buildResult =
        pipeline.build()

    val artifact =
        FoodsKnowledgeArtifactGenerator().generate(
            candidates = buildResult.validCandidates
        )

    FoodsKnowledgeArtifactBuildSummaryPrinter()
        .print(
            buildResult = buildResult,
            artifact = artifact
        )

    val comparison =
        FoodsKnowledgeArtifactComparator()
            .compare(
                existingFoods = existingFoods,
                generatedArtifact = artifact
            )

    FoodsKnowledgeArtifactComparisonPrinter()
        .print(comparison)
}