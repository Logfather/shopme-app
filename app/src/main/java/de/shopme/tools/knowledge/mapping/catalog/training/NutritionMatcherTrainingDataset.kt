package de.shopme.tools.knowledge.mapping.catalog.training

data class NutritionMatcherTrainingDataset(
    val version: Int = 1,
    val datasetType: String =
        "NUTRITION_CATALOG_SERVER_MATCHER",
    val summary:
    NutritionMatcherTrainingDatasetSummary,
    val examples:
    List<NutritionMatcherTrainingExample>
)

data class NutritionMatcherTrainingDatasetSummary(
    val sourceCatalogKeyCount: Int,
    val exampleCount: Int,
    val positiveCount: Int,
    val negativeCount: Int,
    val acceptedOriginalMatchCount: Int,
    val acceptedSelectedCount: Int,
    val rejectedSelectedCount: Int,
    val rejectedNoMatchCandidateCount: Int,
    val nonSelectedAlternativeCount: Int
)

data class ExportNutritionMatcherTrainingDatasetResult(
    val dataset: NutritionMatcherTrainingDataset,
    val outputFile: String
)