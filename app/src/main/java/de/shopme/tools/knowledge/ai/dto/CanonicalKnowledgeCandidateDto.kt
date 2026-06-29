package de.shopme.tools.knowledge.ai.dto

data class CanonicalKnowledgeCandidateDto(

    val canonicalId: String?,

    val aliases: List<String>?,

    val dimensions: List<KnowledgeDimensionCandidateDto>?,

    val metadata: CandidateMetadataDto?

)