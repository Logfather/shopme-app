package de.shopme.tools.knowledge.ai.builder.taxonomy

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyEntry
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyKnowledge

class MergedCandidateFoodTaxonomyKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): FoodTaxonomyKnowledge {
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
                                        KnowledgeDimensionCandidateType.TAXONOMY
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val parent =
                        payload.toParentCategory()
                            ?: return@mapNotNull null

                    key to FoodTaxonomyEntry(
                        parent = parent
                    )
                }
                .toMap()
                .toSortedMap()

        return FoodTaxonomyKnowledge(
            entries = entries
        )
    }

    private fun Any.toParentCategory(): String? {
        if (this is FoodTaxonomyEntry) {
            return parent
                .normalizeCategory()
                .takeIf { it.isNotBlank() }
        }

        if (this !is Map<*, *>) {
            return null
        }

        val mainCategory =
            (this["mainCategory"] as? String)
                ?.normalizeCategory()
                ?.takeIf { it.isNotBlank() }

        if (mainCategory != null) {
            return mainCategory
        }

        val hierarchy =
            (this["hierarchy"] as? List<*>)
                ?.mapNotNull { it as? String }
                ?.map { it.normalizeCategory() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

        if (hierarchy.isNotEmpty()) {
            return hierarchy.last()
        }

        val categories =
            (this["categories"] as? List<*>)
                ?.mapNotNull { it as? String }
                ?.map { it.normalizeCategory() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

        return categories.lastOrNull()
    }

    private fun String.normalizeCategory(): String {
        return removePrefix("en:")
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}