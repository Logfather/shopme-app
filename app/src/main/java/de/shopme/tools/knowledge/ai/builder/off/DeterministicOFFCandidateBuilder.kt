package de.shopme.tools.knowledge.ai.builder.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class DeterministicOFFCandidateBuilder(
    private val extractor: RawOFFCandidateExtractor = RawOFFCandidateExtractor()
) : AIKnowledgeBuilder {

    override fun build(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult {
        return AIKnowledgeBuildResult(
            candidates = request.inputs.mapNotNull { input ->

                val data = extractor.extract(input)

                if (data.sourceId.isBlank()) {
                    return@mapNotNull null
                }

                if (data.name.isNullOrBlank()) {
                    return@mapNotNull null
                }

                CanonicalKnowledgeCandidate(
                    canonicalId = data.sourceId,
                    aliases = setOf(data.name),
                    dimensions = buildList {

                        addIfPresent(KnowledgeDimensionCandidateType.NUTRITION, data.nutrition)
                        addIfPresent(KnowledgeDimensionCandidateType.CARBON, data.carbon)
                        addIfPresent(KnowledgeDimensionCandidateType.WATER, data.water)
                        addIfPresent(KnowledgeDimensionCandidateType.WATER_STRESS, data.waterStress)
                        addIfPresent(KnowledgeDimensionCandidateType.GLYCEMIC, data.glycemic)
                        addIfPresent(KnowledgeDimensionCandidateType.ALLERGENS, data.allergens)
                        addIfPresent(KnowledgeDimensionCandidateType.INGREDIENTS, data.ingredients)
                        addIfPresent(KnowledgeDimensionCandidateType.TAXONOMY, data.taxonomy)
                        addIfPresent(KnowledgeDimensionCandidateType.SEASONALITY, data.seasonality)
                        addIfPresent(KnowledgeDimensionCandidateType.PACKAGING, data.packaging)
                        addIfPresent(KnowledgeDimensionCandidateType.FAIRTRADE, data.fairtrade)
                        addIfPresent(KnowledgeDimensionCandidateType.ANIMAL_WELFARE, data.animalWelfare)
                        addIfPresent(KnowledgeDimensionCandidateType.BIODIVERSITY, data.biodiversity)
                        addIfPresent(KnowledgeDimensionCandidateType.POLLINATOR, data.pollinator)
                        addIfPresent(KnowledgeDimensionCandidateType.LOCALITY, data.locality)
                        addIfPresent(KnowledgeDimensionCandidateType.FOOD_MILES, data.foodMiles)
                        addIfPresent(KnowledgeDimensionCandidateType.PRODUCTION, data.production)
                        addIfPresent(KnowledgeDimensionCandidateType.PROCESSING, data.processing)
                        addIfPresent(KnowledgeDimensionCandidateType.RECIPE, data.recipe)
                        addIfPresent(KnowledgeDimensionCandidateType.INGREDIENT_GRAPH, data.ingredientGraph)
                        addIfPresent(KnowledgeDimensionCandidateType.RECIPE_GRAPH, data.recipeGraph)
                    },
                    metadata = CandidateMetadata(
                        source = request.source.name,
                        sourceId = data.sourceId,
                        confidence = 1.0,
                        version = request.source.version
                    )
                )
            }
        )
    }

    private fun MutableList<KnowledgeDimensionCandidate>.addIfPresent(
        type: KnowledgeDimensionCandidateType,
        payload: Any?
    ) {
        if (payload != null) {
            add(
                KnowledgeDimensionCandidate(
                    dimension = type,
                    payload = payload
                )
            )
        }
    }
}