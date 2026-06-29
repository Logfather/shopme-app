package de.shopme.tools.knowledge.ki_candidates

data class CanonicalKnowledgeCandidate(

    val canonicalId: String,

    val aliases: Set<String>,

    val dimensions: List<KnowledgeDimensionCandidate>,

    val metadata: CandidateMetadata
)