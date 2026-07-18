package de.shopme.tools.knowledge.ai.builder.ingredientgraph

import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphEntry
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateIngredientGraphKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): IngredientGraphKnowledge {

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
                                        KnowledgeDimensionCandidateType.INGREDIENT_GRAPH
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val entry =
                        toIngredientGraphEntry(payload)
                            ?: return@mapNotNull null

                    key to entry
                }
                .toMap()
                .toSortedMap()

        return IngredientGraphKnowledge(
            entries = entries
        )
    }

    private fun toIngredientGraphEntry(
        payload: Any?
    ): IngredientGraphEntry? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val value =
            map["ingredients"]
                ?: map["ingredientIds"]
                ?: map["references"]
                ?: return null

        val ingredients =
            when (value) {

                is List<*> ->
                    value
                        .mapNotNull {
                            it as? String
                        }

                is Set<*> ->
                    value
                        .mapNotNull {
                            it as? String
                        }

                is String ->
                    listOf(value)

                else ->
                    return null
            }

        val normalized =
            ingredients
                .map {
                    it.trim()
                        .lowercase()
                        .replace("-", " ")
                        .replace("_", " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                }
                .filter {
                    it.isNotBlank()
                }
                .toSortedSet()

        if (normalized.isEmpty()) {
            return null
        }

        return IngredientGraphEntry(
            ingredients = normalized
        )
    }
}