package de.shopme.tools.knowledge.ai.dto

data class CanonicalKnowledgeCandidateAIResponseDto(

    val schemaVersion: String?,

    val candidates: List<CanonicalKnowledgeCandidateDto>

)