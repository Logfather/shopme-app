package de.shopme.tools.knowledge.ai.builder.ingredients

import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateIngredientsKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): IngredientsKnowledge {
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
                                        KnowledgeDimensionCandidateType.INGREDIENTS
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val ingredients =
                        payload.toIngredients()
                            .takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                    key to ingredients
                }
                .toMap()
                .toSortedMap()

        return IngredientsKnowledge(
            entries = entries
        )
    }

    private fun Any.toIngredients(): Set<String> {
        if (this is Set<*>) {
            return this
                .mapNotNull { it as? String }
                .normalizeIngredients()
        }

        if (this is List<*>) {
            return this
                .mapNotNull { it as? String }
                .normalizeIngredients()
        }

        if (this is Map<*, *>) {
            val structuredIngredients =
                (this["ingredients"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

            if (structuredIngredients.isNotEmpty()) {
                return structuredIngredients.normalizeIngredients()
            }

            val ingredientsText =
                this["ingredientsText"] as? String

            return ingredientsText
                ?.split(",", ";", ".", "•")
                ?.normalizeIngredients()
                ?: emptySet()
        }

        return emptySet()
    }

    private fun List<String>.normalizeIngredients(): Set<String> {
        return map { ingredient ->
            ingredient
                .trim()
                .lowercase()
                .removePrefix("en:")
                .replace("-", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
            .filter { it.isNotBlank() }
            .toSortedSet()
    }
}