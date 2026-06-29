package de.shopme.tools.knowledge.source

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

interface KnowledgeSourceAdapter {

    fun load(): List<CanonicalKnowledgeCandidate>

}