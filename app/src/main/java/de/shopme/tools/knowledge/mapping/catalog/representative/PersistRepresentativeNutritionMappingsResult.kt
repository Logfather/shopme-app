package de.shopme.tools.knowledge.mapping.catalog.representative

data class PersistRepresentativeNutritionMappingsResult(
    val validationEntryCount: Int,
    val acceptedValidationCount: Int,
    val existingMappingCount: Int,
    val addedMappingCount: Int,
    val unchangedMappingCount: Int,
    val finalMappingCount: Int,
    val outputMappingFile: String
)