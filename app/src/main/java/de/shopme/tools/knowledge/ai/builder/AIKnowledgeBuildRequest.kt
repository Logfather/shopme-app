package de.shopme.tools.knowledge.ai.builder

data class AIKnowledgeBuildRequest(
    val source: AIKnowledgeSourceInfo,
    val inputs: List<RawKnowledgeInput>
)