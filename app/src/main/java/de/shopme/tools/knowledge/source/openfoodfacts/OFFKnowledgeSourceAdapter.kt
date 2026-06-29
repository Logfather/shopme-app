package de.shopme.tools.knowledge.source.openfoodfacts

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceAdapter

class OFFKnowledgeSourceAdapter(
    private val candidates: List<OFFKnowledgeCandidate>,
    private val mapper: OFFCandidateMapper
) : KnowledgeSourceAdapter {

    override fun load(): List<CanonicalKnowledgeCandidate> =
        candidates.map(mapper::map)

}