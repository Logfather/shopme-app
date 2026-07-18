package de.shopme.tools.knowledge.ai.builder.environment

import de.shopme.tools.knowledge.environment.EnvironmentalImpact
import de.shopme.tools.knowledge.environment.EnvironmentalImpactKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateEnvironmentalImpactKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): EnvironmentalImpactKnowledge {
        val entries = candidates
            .mapNotNull { candidate ->
                val payload = candidate.dimensions
                    .firstOrNull {
                        it.dimension == KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT
                    }
                    ?.payload
                    ?: return@mapNotNull null

                val impact = payload.toEnvironmentalImpact()
                    ?: return@mapNotNull null

                val key =
                    candidate.canonicalId.trim()

                if (key.isBlank()) {
                    return@mapNotNull null
                }

                key to impact
            }
            .toMap()
            .toSortedMap()

        return EnvironmentalImpactKnowledge(entries)
    }

    private fun Any.toEnvironmentalImpact(): EnvironmentalImpact? {
        if (this is EnvironmentalImpact) {
            return this
        }

        if (this !is Map<*, *>) {
            return null
        }

        return EnvironmentalImpact(
            environmentScoreMptPerKg = double("environmentScoreMptPerKg"),
            climateKgCo2EqPerKg = double("climateKgCo2EqPerKg"),
            landUsePtPerKg = double("landUsePtPerKg"),
            waterDeprivationM3PerKg = double("waterDeprivationM3PerKg")
        )
    }

    private fun Map<*, *>.double(
        key: String
    ): Double {
        return (this[key] as? Number)
            ?.toDouble()
            ?: 0.0
    }
}