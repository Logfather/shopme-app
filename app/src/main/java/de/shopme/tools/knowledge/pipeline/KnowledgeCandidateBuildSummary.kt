package de.shopme.tools.knowledge.pipeline

data class KnowledgeCandidateBuildSummary(

    val loadedCandidates: Int,

    val validCandidates: Int,

    val rejectedCandidates: Int

)