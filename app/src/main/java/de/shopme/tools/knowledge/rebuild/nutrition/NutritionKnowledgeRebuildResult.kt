package de.shopme.tools.knowledge.rebuild.nutrition

data class NutritionKnowledgeRebuildResult(
    val version: Int = 1,
    val mode: NutritionKnowledgeRebuildMode,
    val before: NutritionKnowledgeRebuildSnapshot,
    val matching: NutritionKnowledgeRebuildMatchingResult,
    val persistence:
    NutritionKnowledgeRebuildPersistenceResult,
    val after: NutritionKnowledgeRebuildSnapshot,
    val delta: NutritionKnowledgeRebuildDelta,
    val files: NutritionKnowledgeRebuildFiles
)

data class NutritionKnowledgeRebuildDelta(
    val mappingCount: Int,
    val coveredCatalogItemCount: Int,
    val missingCatalogItemCount: Int,
    val coverage: Double
)

data class NutritionKnowledgeRebuildFiles(
    val catalogFile: String,
    val nutritionArtifactFile: String,
    val requestFile: String,
    val decisionFile: String,
    val validationFile: String,
    val mappingFile: String,
    val resultFile: String
)