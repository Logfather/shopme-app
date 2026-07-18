package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.ai.builder.off.DeterministicOFFCandidateBuilder
import de.shopme.tools.knowledge.compiler.catalog.DefaultFileCatalogUpdateWorkflowFactory
import java.io.File

object DefaultAIKnowledgeBuilderPipelineFactory {

    fun create(
        catalogFile: File
    ): DefaultAIKnowledgeBuilderPipeline {
        return create(
            catalogFile = catalogFile,
            buildersBySourceType = defaultBuildersBySourceType()
        )
    }

    fun create(
        catalogFile: File,
        buildersBySourceType: Map<AIKnowledgeSourceType, AIKnowledgeBuilder>
    ): DefaultAIKnowledgeBuilderPipeline {

        val resolver =
            DefaultAIKnowledgeBuilderResolver(
                buildersBySourceType = buildersBySourceType
            )

        val catalogUpdateWorkflow =
            DefaultFileCatalogUpdateWorkflowFactory.create(
                file = catalogFile
            )

        return DefaultAIKnowledgeBuilderPipeline(
            builderResolver = resolver,
            catalogUpdateWorkflow = catalogUpdateWorkflow
        )
    }

    private fun defaultBuildersBySourceType():
            Map<AIKnowledgeSourceType, AIKnowledgeBuilder> {
        return mapOf(
            AIKnowledgeSourceType.OPEN_FOOD_FACTS to
                    DeterministicOFFCandidateBuilder(),

            AIKnowledgeSourceType.AGRIBALYSE to
                    AgribalyseCanonicalCandidateBuilder()
        )
    }
}