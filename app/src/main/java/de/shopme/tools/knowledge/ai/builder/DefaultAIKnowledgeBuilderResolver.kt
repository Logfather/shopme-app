package de.shopme.tools.knowledge.ai.builder

class DefaultAIKnowledgeBuilderResolver(
    private val buildersBySourceType:
    Map<AIKnowledgeSourceType, AIKnowledgeBuilder>
) : AIKnowledgeBuilderResolver {

    override fun resolve(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuilder {

        return buildersBySourceType[
            request.source.type
        ]
            ?: throw IllegalArgumentException(
                "No AIKnowledgeBuilder registered for source type ${request.source.type}"
            )
    }
}