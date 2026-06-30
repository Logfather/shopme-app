package de.shopme.tools.knowledge.ai.builder

class DefaultAIKnowledgeBuilder(
    private val extractor: AIKnowledgeCandidateExtractor
) : AIKnowledgeBuilder {

    override fun build(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult {

        require(
            request.inputs.isNotEmpty()
        ) {
            "Missing AI knowledge build inputs."
        }

        return AIKnowledgeBuildResult(
            candidates = extractor.extract(request)
        )
    }
}