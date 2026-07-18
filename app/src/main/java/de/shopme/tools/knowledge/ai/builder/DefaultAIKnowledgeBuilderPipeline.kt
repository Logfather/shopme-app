package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogResultImporter
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger

class DefaultAIKnowledgeBuilderPipeline(
    private val builderResolver: AIKnowledgeBuilderResolver,
    private val catalogUpdateWorkflow: AIKnowledgeCatalogResultImporter,
    private val candidateMerger: KnowledgeCandidateMerger = KnowledgeCandidateMerger()
) {

    fun run(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult {

        val builder =
            builderResolver.resolve(request)

        val buildResult =
            builder.build(request)

        val mergeResult =
            candidateMerger.merge(buildResult.candidates)

        if (mergeResult.conflicts.isNotEmpty()) {
            println("AI knowledge merge conflicts: ${mergeResult.conflicts.size}")

            mergeResult.conflicts.forEach { conflict ->
                println(conflict)
            }
        }

        val mergedResult =
            AIKnowledgeBuildResult(
                candidates = mergeResult.candidates
            )

        catalogUpdateWorkflow.importAIKnowledge(mergedResult)

        return mergedResult
    }
}