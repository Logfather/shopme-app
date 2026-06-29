package de.shopme.tools.knowledge.catalog.report

data class CatalogNutritionNormalizationReport(
    val totalItems: Int,
    val knownReferences: Int,
    val unknownReferences: Int,
    val unknownNames: Map<String, Int> = emptyMap()
) {

    val coveragePercent: Double
        get() =
            if (totalItems == 0) {
                0.0
            } else {
                knownReferences * 100.0 / totalItems
            }
}