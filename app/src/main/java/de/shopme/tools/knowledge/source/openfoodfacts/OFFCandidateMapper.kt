package de.shopme.tools.knowledge.source.openfoodfacts

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateTypeMapper
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidate

class OFFCandidateMapper {

    fun map(
        candidate: OFFKnowledgeCandidate
    ): CanonicalKnowledgeCandidate {

        return CanonicalKnowledgeCandidate(
            canonicalId = candidate.catalogNormalizedName,
            aliases = setOf(candidate.offProductName),
            dimensions = candidate.dimensions.map { dimensionId ->

                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateTypeMapper.map(dimensionId),
                    payload = when (dimensionId) {
                        KnowledgeDimensionId.NUTRITION ->
                            candidate.nutritionFacts ?: Unit

                        else ->
                            Unit
                    }
                )
            },
            metadata = CandidateMetadata(
                source = candidate.source,
                sourceId = candidate.offCode,
                confidence = 1.0,
                version = null
            )
        )
    }
}