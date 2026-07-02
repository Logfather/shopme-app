package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.catalog.FileCatalogUpdateWorkflow

class DefaultAIKnowledgeBuilderPipeline(
    private val builderResolver: AIKnowledgeBuilderResolver,
    private val catalogUpdateWorkflow: FileCatalogUpdateWorkflow
) {

    fun run(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult {

        val builder =
            builderResolver.resolve(request)

        val result =
            builder.build(request)

        catalogUpdateWorkflow.importAIKnowledge(result)

        return result
    }
}