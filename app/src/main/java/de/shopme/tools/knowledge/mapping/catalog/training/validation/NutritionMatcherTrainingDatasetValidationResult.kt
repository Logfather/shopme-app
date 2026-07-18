package de.shopme.tools.knowledge.mapping.catalog.training.validation

data class NutritionMatcherTrainingDatasetValidationResult(
    val valid: Boolean,
    val datasetVersion: Int,
    val datasetType: String,
    val exampleCount: Int,
    val positiveCount: Int,
    val negativeCount: Int,
    val issueCount: Int,
    val issues:
    List<NutritionMatcherTrainingDatasetValidationIssue>,
    val validatedFile: String
)

data class NutritionMatcherTrainingDatasetValidationIssue(
    val code:
    NutritionMatcherTrainingDatasetValidationIssueCode,
    val exampleId: String?,
    val catalogKey: String?,
    val message: String
)

enum class NutritionMatcherTrainingDatasetValidationIssueCode {
    UNSUPPORTED_VERSION,
    INVALID_DATASET_TYPE,

    SUMMARY_EXAMPLE_COUNT_MISMATCH,
    SUMMARY_POSITIVE_COUNT_MISMATCH,
    SUMMARY_NEGATIVE_COUNT_MISMATCH,
    SUMMARY_CATALOG_KEY_COUNT_MISMATCH,
    SUMMARY_ACCEPTED_SELECTED_COUNT_MISMATCH,
    SUMMARY_REJECTED_SELECTED_COUNT_MISMATCH,
    SUMMARY_NO_MATCH_COUNT_MISMATCH,
    SUMMARY_ALTERNATIVE_COUNT_MISMATCH,

    DUPLICATE_EXAMPLE_ID,
    DUPLICATE_CANDIDATE_PAIR,
    INVALID_STABLE_ID,
    NON_DETERMINISTIC_ORDER,

    INVALID_CATALOG_KEY,
    INVALID_SERVER_KEY,
    INVALID_SERVER_ARTIFACT,

    INVALID_CANDIDATE_RANK,
    INVALID_CANDIDATE_COUNT,
    INVALID_DIAGNOSTIC_SCORE,
    INVALID_MATCHER_CONFIDENCE,
    INVALID_TRAINING_WEIGHT,

    DUPLICATE_SHARED_TOKEN,
    UNSORTED_SHARED_TOKENS,
    DUPLICATE_REPRESENTATIVE_REASON,
    UNSORTED_REPRESENTATIVE_REASONS,

    INVALID_POSITIVE_ROLE,
    INVALID_NEGATIVE_ROLE,
    INVALID_SELECTED_STATE,
    INVALID_REPRESENTATIVE_DECISION,
    INVALID_NO_MATCH_STATUS,

    INVALID_PROVENANCE,

    SUMMARY_ACCEPTED_ORIGINAL_COUNT_MISMATCH,
    INVALID_DIAGNOSTIC_SCORE_AVAILABILITY,
}