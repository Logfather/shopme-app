package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class AgribalyseCanonicalCandidateBuilder(
    private val extractor: AgribalyseCandidateExtractor = AgribalyseCandidateExtractor()
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
                        addIfPresent(KnowledgeDimensionCandidateType.CARBON, data.carbon)
                        addIfPresent(KnowledgeDimensionCandidateType.WATER, data.water)
                        addIfPresent(KnowledgeDimensionCandidateType.PRODUCTION, data.production)
                        addIfPresent(KnowledgeDimensionCandidateType.TAXONOMY, data.taxonomy)
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