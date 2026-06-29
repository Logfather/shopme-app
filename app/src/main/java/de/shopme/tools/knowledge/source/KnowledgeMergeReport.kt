package de.shopme.tools.knowledge.source

data class KnowledgeMergeReport<T : KnowledgeCandidate>(

    val totalCandidates: Int,

    val mergedReferences: Int,

    val conflicts: List<KnowledgeConflict<T>>

)