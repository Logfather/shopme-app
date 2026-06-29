package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class AIKnowledgeBuildResult(
    val candidates: List<CanonicalKnowledgeCandidate>
)