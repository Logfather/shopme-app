package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

class OFFKnowledgeCandidateRanker {

    fun rank(
        candidates: List<OFFKnowledgeCandidate>
    ): List<OFFKnowledgeCandidate> {

        return candidates
            .groupBy {
                it.catalogNormalizedName
            }
            .values
            .mapNotNull {
                rankBest(it)
            }
            .sortedBy {
                it.catalogNormalizedName
            }
    }

    private fun rankBest(
        candidates: List<OFFKnowledgeCandidate>
    ): OFFKnowledgeCandidate? {

        return candidates
            .maxWithOrNull(
                compareBy<OFFKnowledgeCandidate> {
                    it.dimensions.size
                }.thenBy {
                    it.score()
                }.thenBy {
                    it.offCode ?: ""
                }
            )
    }

    private fun OFFKnowledgeCandidate.score(): Int {

        var score = 0

        dimensions.forEach { dimension ->

            score += when (dimension) {

                KnowledgeDimensionId.NUTRITION -> 100
                KnowledgeDimensionId.INGREDIENTS -> 90
                KnowledgeDimensionId.NUTRI_SCORE -> 80
                KnowledgeDimensionId.ALLERGENS -> 70
                KnowledgeDimensionId.CARBON -> 60
                KnowledgeDimensionId.FOOD_TAXONOMY -> 50
                KnowledgeDimensionId.PROCESSING -> 40
                KnowledgeDimensionId.PRODUCTION -> 30
                KnowledgeDimensionId.PACKAGING -> 20
                KnowledgeDimensionId.LOCALITY -> 10
                KnowledgeDimensionId.FOOD_MILES -> 10

                else -> 0
            }
        }

        if (!offCode.isNullOrBlank()) {
            score += 5
        }

        return score
    }
}