package de.shopme.tools.knowledge.rebuild.nutrition

data class NutritionKnowledgeRebuildMatchingResult(
    val requestCount: Int,
    val previouslyCompletedCount: Int,
    val processedCount: Int,
    val localModelDecisionCount: Int,
    val chatGptDecisionCount: Int,
    val gptFallbackRequiredCount: Int,
    val matchCount: Int,
    val noMatchCount: Int,
    val errorCount: Int
) {

    init {
        require(requestCount >= 0)
        require(previouslyCompletedCount >= 0)
        require(processedCount >= 0)
        require(localModelDecisionCount >= 0)
        require(chatGptDecisionCount >= 0)
        require(gptFallbackRequiredCount >= 0)
        require(matchCount >= 0)
        require(noMatchCount >= 0)
        require(errorCount >= 0)

        require(
            localModelDecisionCount +
                    chatGptDecisionCount +
                    gptFallbackRequiredCount +
                    errorCount <=
                    requestCount
        ) {
            "Matching outcome count exceeds request count."
        }
        require(
            previouslyCompletedCount +
                    processedCount ==
                    requestCount
        ) {
            "Previously completed plus processed must equal " +
                    "requestCount."
        }
    }
}