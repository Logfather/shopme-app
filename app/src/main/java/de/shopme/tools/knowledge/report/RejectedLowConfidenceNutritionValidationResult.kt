package de.shopme.tools.knowledge.report

data class RejectedLowConfidenceNutritionValidationResult(
    val report:
    RejectedLowConfidenceNutritionValidationReport,
    val outputFile: String
)

data class RejectedLowConfidenceNutritionValidationReport(
    val version: Int = 1,
    val summary:
    RejectedLowConfidenceNutritionValidationSummary,
    val entries:
    List<RejectedLowConfidenceNutritionValidationEntry>
)

data class RejectedLowConfidenceNutritionValidationSummary(
    val rejectedLowConfidenceCount: Int,
    val identicalCount: Int,
    val representativeCount: Int,
    val incompatibleCount: Int,
    val acceptedCount: Int,
    val stillRejectedCount: Int
)