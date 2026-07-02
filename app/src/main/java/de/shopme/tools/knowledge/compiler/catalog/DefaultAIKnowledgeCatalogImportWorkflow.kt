package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeCandidateProcessor
import de.shopme.tools.knowledge.compiler.candidate.CanonicalKnowledgeCandidateToPatchOperationMapper
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch

class DefaultAIKnowledgeCatalogImportWorkflow(
    private val candidateProcessor: AIKnowledgeCandidateProcessor,
    private val patchOperationMapper: CanonicalKnowledgeCandidateToPatchOperationMapper,
    private val catalogUpdateWorkflow: AIKnowledgeCatalogUpdateWorkflow
) : AIKnowledgeCatalogImportWorkflow {

    override fun importAIKnowledge(
        catalog: List<CatalogItem>,
        result: AIKnowledgeBuildResult
    ) {

        val importBatch =
            candidateProcessor.process(result)

        val patch =
            FoodsJsonPatch(
                operations = importBatch.candidates.map { candidate ->
                    patchOperationMapper.map(
                        catalog = catalog,
                        candidate = candidate
                    )
                }
            )

        catalogUpdateWorkflow.updateCatalog(
            catalog = catalog,
            patch = patch
        )
    }
}