package de.shopme.tools.knowledge.rebuild.nutrition.coverage

data class NutritionCoverageGapGroupedAnalysis(
    val version: Int,
    val totalGapCount: Int,
    val typeGroupCount: Int,
    val groups: List<NutritionCoverageGapTypeAnalysis>
) {

    init {
        require(version > 0) {
            "version must be positive."
        }

        require(totalGapCount >= 0) {
            "totalGapCount must not be negative."
        }

        require(typeGroupCount >= 0) {
            "typeGroupCount must not be negative."
        }

        require(typeGroupCount == groups.size) {
            "typeGroupCount must equal groups.size."
        }

        require(
            groups.sumOf {
                it.count
            } == totalGapCount
        ) {
            "All type groups must cover all nutrition coverage gaps."
        }

        require(
            groups.map {
                it.type
            }.distinct().size == groups.size
        ) {
            "Coverage-gap type groups must be unique."
        }
    }
}

data class NutritionCoverageGapTypeAnalysis(
    val type: String,
    val count: Int,
    val percentage: Double,
    val requestExistsCount: Int,
    val requestMissingCount: Int,
    val decisionExistsCount: Int,
    val decisionMissingCount: Int,
    val mappingExistsCount: Int,
    val mappingMissingCount: Int,
    val averageCandidateCount: Double?,
    val averageDecisionConfidence: Double?,
    val averageTopCandidateScore: Double?,
    val averageTopScoreDelta: Double?,
    val decisionTypeCounts: Map<String, Int>,
    val decisionSourceCounts: Map<String, Int>,
    val examples: List<NutritionCoverageGapTypeExample>
) {

    init {
        require(type.isNotBlank()) {
            "type must not be blank."
        }

        require(count > 0) {
            "count must be positive."
        }

        require(percentage >= 0.0) {
            "percentage must not be negative."
        }

        require(requestExistsCount + requestMissingCount == count) {
            "Request counts must cover the whole group '$type'."
        }

        require(decisionExistsCount + decisionMissingCount == count) {
            "Decision counts must cover the whole group '$type'."
        }

        require(mappingExistsCount + mappingMissingCount == count) {
            "Mapping counts must cover the whole group '$type'."
        }

        require(
            decisionTypeCounts.values.sum() == count
        ) {
            "Decision-type counts must cover the whole group '$type'."
        }

        require(
            decisionSourceCounts.values.sum() == count
        ) {
            "Decision-source counts must cover the whole group '$type'."
        }

        require(examples.size <= 10) {
            "A type group must contain at most ten examples."
        }
    }
}

data class NutritionCoverageGapTypeExample(
    val catalogKey: String,
    val decisionType: String?,
    val decisionSource: String?,
    val decisionConfidence: Double?,
    val candidateCount: Int,
    val topCandidateKey: String?,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val topCandidateSharedTokens: List<String>,
    val details: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(candidateCount >= 0) {
            "candidateCount must not be negative."
        }

        require(details.isNotBlank()) {
            "details must not be blank."
        }
    }
}