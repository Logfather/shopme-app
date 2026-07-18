package de.shopme.tools.knowledge.mapping.catalog.training

data class RepresentativeNutritionMappingTrainingDataset(
    val version: Int = 1,
    val datasetType: String =
        "REPRESENTATIVE_NUTRITION_MAPPING",
    val summary:
    RepresentativeNutritionMappingTrainingDatasetSummary,
    val examples:
    List<RepresentativeNutritionMappingTrainingExample>
)

data class RepresentativeNutritionMappingTrainingDatasetSummary(
    val sourceEntryCount: Int,
    val exportedExampleCount: Int,
    val identicalCount: Int,
    val representativeCount: Int
)

data class ExportRepresentativeNutritionMappingTrainingExamplesResult(
    val dataset:
    RepresentativeNutritionMappingTrainingDataset,
    val outputFile: String
)