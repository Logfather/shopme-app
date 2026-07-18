package de.shopme.tools.knowledge.mapping.catalog.representative

enum class RepresentativeNutritionMappingDecisionType {

    IDENTICAL,

    REPRESENTATIVE,

    INCOMPATIBLE
}

enum class RepresentativeNutritionMappingReason {

    EXACT_NORMALIZED_KEY,

    SAME_PRODUCT_CLASS,

    COMPATIBLE_SPECIALIZATION,

    COMPATIBLE_VARIANT,

    COMPATIBLE_PREPARATION,

    CRITICAL_MODIFIER_CONFLICT,

    PRODUCT_CLASS_CONFLICT,

    PRODUCT_FORM_CONFLICT,

    PROCESSING_STATE_CONFLICT,

    INSUFFICIENT_EVIDENCE
}

data class RepresentativeNutritionMappingRequest(
    val catalogKey: String,
    val serverKey: String,
    val confidence: Double,
    val candidateRank: Int,
    val diagnosticScore: Double? = null,
    val sharedTokens: List<String> = emptyList()
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(serverKey.isNotBlank()) {
            "serverKey must not be blank."
        }

        require(confidence in 0.0..1.0) {
            "confidence must be between 0.0 and 1.0."
        }

        require(candidateRank >= 1) {
            "candidateRank must be at least 1."
        }

        require(
            diagnosticScore == null ||
                    diagnosticScore in 0.0..1.0
        ) {
            "diagnosticScore must be between 0.0 and 1.0."
        }
    }

    fun normalized(): RepresentativeNutritionMappingRequest =
        copy(
            catalogKey =
                catalogKey.trim(),
            serverKey =
                serverKey.trim(),
            sharedTokens =
                sharedTokens
                    .asSequence()
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
                    .sorted()
                    .toList()
        )
}

data class RepresentativeNutritionMappingDecision(
    val catalogKey: String,
    val serverKey: String,
    val type: RepresentativeNutritionMappingDecisionType,
    val reasons: List<RepresentativeNutritionMappingReason>
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(serverKey.isNotBlank()) {
            "serverKey must not be blank."
        }

        require(reasons.isNotEmpty()) {
            "reasons must not be empty."
        }

        require(
            reasons.size ==
                    reasons.distinct().size
        ) {
            "reasons must not contain duplicates."
        }

        require(
            reasons ==
                    reasons.sortedBy {
                        it.ordinal
                    }
        ) {
            "reasons must be sorted deterministically."
        }
    }

    val accepted: Boolean
        get() =
            type !=
                    RepresentativeNutritionMappingDecisionType.INCOMPATIBLE
}

data class RepresentativeNutritionMappingValidationResult(
    val decisions: List<RepresentativeNutritionMappingDecision>
) {

    init {
        val duplicateCatalogKeys =
            decisions
                .groupingBy {
                    normalizeKey(
                        value = it.catalogKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys
                .sorted()

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate representative nutrition decisions: " +
                    duplicateCatalogKeys.joinToString()
        }

        require(
            decisions ==
                    decisions.sortedBy {
                        normalizeKey(
                            value = it.catalogKey
                        )
                    }
        ) {
            "decisions must be sorted deterministically by catalogKey."
        }
    }

    val acceptedDecisions:
            List<RepresentativeNutritionMappingDecision>
        get() =
            decisions.filter {
                it.accepted
            }

    val rejectedDecisions:
            List<RepresentativeNutritionMappingDecision>
        get() =
            decisions.filterNot {
                it.accepted
            }

    val acceptedCount: Int
        get() =
            acceptedDecisions.size

    val rejectedCount: Int
        get() =
            rejectedDecisions.size

    private companion object {

        fun normalizeKey(
            value: String
        ): String =
            value
                .trim()
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )
    }
}