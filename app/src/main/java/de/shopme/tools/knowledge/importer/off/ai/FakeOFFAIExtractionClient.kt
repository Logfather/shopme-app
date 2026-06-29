package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIExtractionClient
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import java.time.Instant

class FakeOFFAIExtractionClient :
    AIExtractionClient<OFFAIExtractionBatch> {

    private fun buildDimensions(
        product: OFFAIExtractionInput
    ): List<KnowledgeDimensionCandidate> {

        val categories =
            product.categories
                ?.takeIf { it.isNotBlank() }
                ?: return emptyList()

        return listOf(
            KnowledgeDimensionCandidate(
                dimension = KnowledgeDimensionCandidateType.TAXONOMY,
                payload = categories
            )
        )
    }

    override fun extract(
        input: OFFAIExtractionBatch
    ): KnowledgeImportBatch {

        val candidates =
            input.products.mapNotNull { product ->

                val canonicalId =
                    product.productNameDe
                        ?: product.productName
                        ?: return@mapNotNull null

                CanonicalKnowledgeCandidate(
                    canonicalId = canonicalId
                        .trim()
                        .lowercase(),
                    aliases = setOfNotNull(
                        product.productName,
                        product.productNameDe
                    ),
                    dimensions = buildDimensions(product),
                    metadata = CandidateMetadata(
                        source = input.source,
                        sourceId = product.code,
                        confidence = 1.0,
                        version = input.sourceVersion
                    )
                )
            }
                .distinctBy {
                    it.canonicalId
                }

        return KnowledgeImportBatch(
            candidates = candidates,
            metadata = KnowledgeImportBatchMetadata(
                source = input.source,
                generatedBy = "fake-off-ai-client",
                generatedAt = Instant.now().toString(),
                promptVersion = "fake-v1"
            )
        )
    }
}