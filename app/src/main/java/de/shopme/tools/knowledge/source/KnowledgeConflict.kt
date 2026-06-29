package de.shopme.tools.knowledge.source

data class KnowledgeConflict<T : KnowledgeCandidate>(

    val reference: String,

    val candidates: List<T>

)