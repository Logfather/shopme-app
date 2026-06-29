package de.shopme.tools.knowledge.artifacts

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class FoodsKnowledgeCandidateSerializer {

    fun serialize(
        candidates: List<CanonicalKnowledgeCandidate>
    ): String {

        val entries =
            candidates
                .sortedBy { candidate ->
                    candidate.canonicalId
                }
                .joinToString(
                    separator = ",\n",
                    prefix = "[\n",
                    postfix = "\n]"
                ) { candidate ->

                    """
                    |  {
                    |    "id": "${candidate.canonicalId}"
                    |  }
                    """.trimMargin()
                }

        return entries
    }
}