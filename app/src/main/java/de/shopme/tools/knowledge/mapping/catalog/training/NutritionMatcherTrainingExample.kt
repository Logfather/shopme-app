package de.shopme.tools.knowledge.mapping.catalog.training

data class NutritionMatcherTrainingExample(
    val id: String,
    val catalogKey: String,
    val serverArtifact: String,
    val serverKey: String,
    val label: NutritionMatcherTrainingLabel,
    val role: NutritionMatcherTrainingExampleRole,
    val selected: Boolean,
    val candidateRank: Int,
    val candidateCount: Int,
    val diagnosticScore: Double,
    val diagnosticScoreAvailable: Boolean,
    val sharedTokens: List<String>,
    val domainMismatchFeatures:
    NutritionDomainMismatchFeatures? = null,
    val matcherConfidence: Double,
    val originalDecisionType: String,
    val originalDecisionReason: String?,
    val originalValidationStatus: String,
    val originalValidationReason: String?,
    val representativeDecisionType: String?,
    val representativeReasons: List<String>,
    val trainingWeight: Double,
    val provenance: NutritionMatcherTrainingProvenance
)

enum class NutritionMatcherTrainingLabel {
    POSITIVE,
    NEGATIVE
}

enum class NutritionMatcherTrainingExampleRole {

    /**
     * Regulär vom GPT-Matcher ausgewählt, vom ursprünglichen
     * Validator akzeptiert und produktiv persistiert.
     */
    ACCEPTED_ORIGINAL_MATCH,

    /**
     * Zunächst wegen niedriger Konfidenz abgelehnt, anschließend
     * vom Representative Validator akzeptiert.
     */
    ACCEPTED_SELECTED,

    /**
     * Vom Matcher ausgewählt, aber vom Representative Validator
     * als inkompatibel verworfen.
     */
    REJECTED_SELECTED,

    /**
     * Kandidat einer vollständigen NO_MATCH-Entscheidung.
     */
    REJECTED_NO_MATCH_CANDIDATE,

    /**
     * Nicht ausgewählte Alternative zu einem ausgewählten Kandidaten.
     */
    NON_SELECTED_ALTERNATIVE
}

data class NutritionMatcherTrainingProvenance(
    val sourceType: String,
    val candidateQualityFile: String,
    val diagnosticsFile: String,
    val representativeValidationFile: String,
    val sourceVersion: Int,
    val matcher: String,
    val validator: String
)