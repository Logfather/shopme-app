package de.shopme.tools.knowledge.ai.builder.recipe

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.recipe.RecipeKnowledge

class MergedCandidateRecipeKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ): RecipeKnowledge =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): RecipeKnowledge {

        val entries =
            candidates
                .mapNotNull { candidate ->

                    val key =
                        candidate.canonicalId.trim()

                    if (key.isBlank()) {
                        return@mapNotNull null
                    }

                    val payload =
                        candidate.dimensions
                            .firstOrNull { dimension ->
                                dimension.dimension ==
                                        KnowledgeDimensionCandidateType.RECIPE
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val recipes =
                        toRecipes(payload)
                            ?: return@mapNotNull null

                    key to recipes
                }
                .toMap()
                .toSortedMap()

        return RecipeKnowledge(
            entries = entries
        )
    }

    private fun toRecipes(
        payload: Any?
    ): List<String>? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val value =
            map["recipes"]
                ?: map["recipeIds"]
                ?: map["references"]
                ?: return null

        val recipes =
            when (value) {

                is List<*> ->
                    value
                        .mapNotNull {
                            it as? String
                        }

                is String ->
                    listOf(value)

                else ->
                    return null
            }

        return recipes
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()
            .takeIf {
                it.isNotEmpty()
            }
    }
}