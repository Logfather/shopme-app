package de.shopme.tools.knowledge.pipeline

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifact
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class FoodsKnowledgeArtifactGenerator :
    KnowledgeArtifactGenerator<FoodsKnowledgeArtifact> {

    override fun generate(
        candidates: List<CanonicalKnowledgeCandidate>
    ): FoodsKnowledgeArtifact {

        return FoodsKnowledgeArtifact(
            candidates = candidates.sortedBy {
                it.canonicalId
            }
        )

    }

}