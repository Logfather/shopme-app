package de.shopme.tools.knowledge.ki_candidates

data class KnowledgeCandidateMergeConflict(
    val canonicalId: String,
    val dimension: KnowledgeDimensionCandidateType,
    val selectedPayload: Any?,
    val rejectedPayloads: List<Any>,
    val resolution: KnowledgeConflictResolutionMetadata? = null
)

data class KnowledgeConflictResolutionMetadata(
    val type: KnowledgeConflictResolutionType,
    val alternatives: Int,
    val selectedScore: Int?,
    val rejectedScores: List<Int>,
    val confidence: KnowledgeConflictResolutionConfidence
)

enum class KnowledgeConflictResolutionType {
    QUALITY_SCORE,
    SOURCE_PRIORITY,
    FIRST_VALUE
}

enum class KnowledgeConflictResolutionConfidence {
    HIGH,
    MEDIUM,
    LOW
}