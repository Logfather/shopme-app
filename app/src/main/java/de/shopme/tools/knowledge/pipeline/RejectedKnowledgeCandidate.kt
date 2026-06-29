package de.shopme.tools.knowledge.pipeline

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class RejectedKnowledgeCandidate(

    val candidate: CanonicalKnowledgeCandidate,

    val validationErrors: List<String>

)