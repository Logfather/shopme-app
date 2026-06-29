package de.shopme.tools.knowledge.ki_candidates

data class CandidateMetadata(

    val source: String,

    val sourceId: String?,

    val confidence: Double,

    val version: String?
)