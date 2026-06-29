package de.shopme.tools.knowledge.source

data class KnowledgeValidationReport<T>(

    val warnings: List<T>

)