package de.shopme.tools.knowledge.ai.builder

data class AIKnowledgeSourceInfo(

    val type: AIKnowledgeSourceType,

    val name: String,

    val version: String? = null
)