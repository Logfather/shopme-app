package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class FoodsKnowledgePatchEntry(

    val canonicalId: String,

    val candidate: CanonicalKnowledgeCandidate

)