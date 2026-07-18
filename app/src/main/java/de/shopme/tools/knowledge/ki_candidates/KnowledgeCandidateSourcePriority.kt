package de.shopme.tools.knowledge.ki_candidates

class KnowledgeCandidateSourcePriority {

    fun priority(
        dimension: KnowledgeDimensionCandidateType,
        source: String
    ): Int {
        val normalizedSource = source.lowercase()

        return when (dimension) {

            KnowledgeDimensionCandidateType.CARBON,
            KnowledgeDimensionCandidateType.WATER,
            KnowledgeDimensionCandidateType.PRODUCTION ->
                when (normalizedSource) {
                    "agribalyse" -> 100
                    "open_food_facts", "off" -> 50
                    else -> 0
                }

            KnowledgeDimensionCandidateType.NUTRITION,
            KnowledgeDimensionCandidateType.INGREDIENTS,
            KnowledgeDimensionCandidateType.ALLERGENS,
            KnowledgeDimensionCandidateType.PACKAGING,
            KnowledgeDimensionCandidateType.PROCESSING ->
                when (normalizedSource) {
                    "open_food_facts", "off" -> 100
                    "agribalyse" -> 50
                    else -> 0
                }

            else -> 0
        }
    }
}