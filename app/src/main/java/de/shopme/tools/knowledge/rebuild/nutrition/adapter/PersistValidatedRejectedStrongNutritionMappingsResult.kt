package de.shopme.tools.knowledge.rebuild.nutrition.adapter

data class PersistValidatedRejectedStrongNutritionMappingsResult(
    val validationEntryCount: Int,
    val acceptedValidationCount: Int,
    val rejectedValidationCount: Int,
    val existingMappingCount: Int,
    val addedMappingCount: Int,
    val unchangedMappingCount: Int,
    val finalMappingCount: Int,
    val validationFile: String,
    val mappingFile: String
) {

    init {
        require(validationEntryCount >= 0) {
            "validationEntryCount must not be negative."
        }

        require(acceptedValidationCount >= 0) {
            "acceptedValidationCount must not be negative."
        }

        require(rejectedValidationCount >= 0) {
            "rejectedValidationCount must not be negative."
        }

        require(existingMappingCount >= 0) {
            "existingMappingCount must not be negative."
        }

        require(addedMappingCount >= 0) {
            "addedMappingCount must not be negative."
        }

        require(unchangedMappingCount >= 0) {
            "unchangedMappingCount must not be negative."
        }

        require(finalMappingCount >= 0) {
            "finalMappingCount must not be negative."
        }

        require(
            acceptedValidationCount +
                    rejectedValidationCount ==
                    validationEntryCount
        ) {
            "Accepted and rejected validations must cover all entries."
        }

        require(
            addedMappingCount +
                    unchangedMappingCount ==
                    acceptedValidationCount
        ) {
            "Added and unchanged mappings must cover all accepted " +
                    "validation entries."
        }

        require(
            finalMappingCount ==
                    existingMappingCount +
                    addedMappingCount
        ) {
            "finalMappingCount must equal existingMappingCount plus " +
                    "addedMappingCount."
        }

        require(validationFile.isNotBlank()) {
            "validationFile must not be blank."
        }

        require(mappingFile.isNotBlank()) {
            "mappingFile must not be blank."
        }
    }
}