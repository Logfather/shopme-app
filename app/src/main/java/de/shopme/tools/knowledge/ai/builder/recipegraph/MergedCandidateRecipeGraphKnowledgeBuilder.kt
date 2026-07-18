package de.shopme.tools.knowledge.ai.builder.recipegraph

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.recipegraph.RecipeGraphEntry
import de.shopme.tools.knowledge.recipegraph.RecipeGraphKnowledge

class MergedCandidateRecipeGraphKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ): RecipeGraphKnowledge =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): RecipeGraphKnowledge {

        val entries =
            candidates
                .mapNotNull { candidate ->

                    val key =
                        candidate.canonicalId
                            .trim()

                    if (key.isBlank()) {
                        return@mapNotNull null
                    }

                    val payload =
                        candidate.dimensions
                            .firstOrNull {
                                it.dimension ==
                                        KnowledgeDimensionCandidateType.RECIPE_GRAPH
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val recipes =
                        payload.toRecipeReferences()

                    if (recipes.isEmpty()) {
                        return@mapNotNull null
                    }

                    key to RecipeGraphEntry(
                        ingredients =
                            recipes.toSet()
                    )
                }
                .toMap()
                .toSortedMap()

        return RecipeGraphKnowledge(
            entries = entries
        )
    }

    private fun Any.toRecipeReferences(): List<String> {

        if (this !is Map<*, *>) {
            return emptyList()
        }

        val value =
            this["recipes"]
                ?: this["recipeIds"]
                ?: this["references"]
                ?: return emptyList()

        val recipes =
            when (value) {

                is List<*> ->
                    value.mapNotNull {
                        it as? String
                    }

                is Set<*> ->
                    value.mapNotNull {
                        it as? String
                    }

                is String ->
                    listOf(value)

                else ->
                    emptyList()
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
    }
}