package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

interface AIKnowledgeCandidateExtractor {

    fun extract(
        request: AIKnowledgeBuildRequest
    ): List<CanonicalKnowledgeCandidate>
}