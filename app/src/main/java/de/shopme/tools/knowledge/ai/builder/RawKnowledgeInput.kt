package de.shopme.tools.knowledge.ai.builder

data class RawKnowledgeInput(
    val sourceId: String,
    val fields: Map<String, Any?>
)