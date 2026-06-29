package de.shopme.tools.knowledge.pipeline

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

interface KnowledgeArtifactGenerator<T> {

    fun generate(
        candidates: List<CanonicalKnowledgeCandidate>
    ): T

}