package de.shopme.tools.knowledge.ai.builder.production

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.production.ProductionKnowledge
import de.shopme.tools.knowledge.production.ProductionMethod

class MergedCandidateProductionKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): ProductionKnowledge {

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
                                        KnowledgeDimensionCandidateType.PRODUCTION
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val methods =
                        payload.toProductionMethods()

                    if (methods.isEmpty()) {
                        return@mapNotNull null
                    }

                    key to methods
                }
                .toMap()
                .toSortedMap()

        return ProductionKnowledge(
            entries = entries
        )
    }

    private fun Any.toProductionMethods(): Set<ProductionMethod> {

        val values =
            when (this) {

                is List<*> ->
                    this.filterIsInstance<String>()

                is Set<*> ->
                    this.filterIsInstance<String>()

                is Map<*, *> ->
                    values
                        .filterIsInstance<List<*>>()
                        .flatten()
                        .filterIsInstance<String>()

                else ->
                    emptyList()
            }

        return values
            .mapNotNull {
                it.toProductionMethod()
            }
            .toSet()
    }

    private fun String.toProductionMethod(): ProductionMethod? =
        runCatching {
            ProductionMethod.valueOf(
                trim()
                    .uppercase()
                    .replace("-", "_")
                    .replace(" ", "_")
            )
        }
            .getOrNull()
}