package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

enum class NutritionMatchPersistenceMissingStage {

    NONE,

    VALIDATION_REJECTED,

    MAPPING,

    RUNTIME
}

data class NutritionMatchPersistenceDiagnostic(
    val catalogKey: String,
    val selectedServerKey: String,
    val decisionSource: String,
    val decisionConfidence: Double,
    val validationRecordPresent: Boolean,
    val validationAccepted: Boolean?,
    val validationStatus: String?,
    val mappingPresent: Boolean,
    val mappedServerKey: String?,
    val runtimePresent: Boolean,
    val firstMissingStage: NutritionMatchPersistenceMissingStage,
    val details: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(selectedServerKey.isNotBlank()) {
            "selectedServerKey must not be blank."
        }

        require(decisionSource.isNotBlank()) {
            "decisionSource must not be blank."
        }

        require(decisionConfidence in 0.0..1.0) {
            "decisionConfidence must be between 0.0 and 1.0."
        }

        require(
            validationRecordPresent ||
                    validationAccepted == null
        ) {
            "A missing validation record must not have an acceptance " +
                    "result."
        }

        require(
            validationRecordPresent ||
                    validationStatus == null
        ) {
            "A missing validation record must not have a validation " +
                    "status."
        }

        require(
            mappingPresent ==
                    (mappedServerKey != null)
        ) {
            "mappingPresent must correspond to mappedServerKey."
        }

        require(
            mappedServerKey == null ||
                    mappedServerKey.isNotBlank()
        ) {
            "mappedServerKey must be null or non-blank."
        }

        require(
            runtimePresent ||
                    firstMissingStage !=
                    NutritionMatchPersistenceMissingStage.NONE
        ) {
            "A missing runtime entry must have a missing stage."
        }

        require(
            firstMissingStage !=
                    NutritionMatchPersistenceMissingStage.MAPPING ||
                    !mappingPresent
        ) {
            "MAPPING may only be the first missing stage when no " +
                    "mapping is present."
        }

        require(
            firstMissingStage !=
                    NutritionMatchPersistenceMissingStage.RUNTIME ||
                    mappingPresent
        ) {
            "RUNTIME may only be missing after a mapping was " +
                    "persisted."
        }

        require(
            firstMissingStage !=
                    NutritionMatchPersistenceMissingStage
                        .VALIDATION_REJECTED ||
                    validationAccepted == false
        ) {
            "VALIDATION_REJECTED requires an explicitly rejected " +
                    "validation result."
        }

        require(details.isNotBlank()) {
            "details must not be blank."
        }
    }

    val fullyPersisted: Boolean
        get() =
            firstMissingStage ==
                    NutritionMatchPersistenceMissingStage.NONE
}