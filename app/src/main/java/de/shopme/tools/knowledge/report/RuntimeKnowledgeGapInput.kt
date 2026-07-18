package de.shopme.tools.knowledge.report

/**
 * Normalisierte Diagnoseinformationen für genau einen fehlenden
 * Runtime-Knowledge-Eintrag.
 *
 * Dieses Modell ist unabhängig von den konkreten Persistenzformaten der
 * Retrieval-, AI- und Validierungspipeline.
 */
data class RuntimeKnowledgeGapInput(
    val catalogKey: String,
    val candidates: List<String> = emptyList(),
    val decision: RuntimeKnowledgeGapDecision? = null,
    val validation: RuntimeKnowledgeGapValidation? = null,
    val serverEntryExists: Boolean? = null
)

/**
 * Normalisierte Entscheidung der AI-Matching-Pipeline.
 */
data class RuntimeKnowledgeGapDecision(
    val outcome: RuntimeKnowledgeGapDecisionOutcome,
    val selectedServerKey: String? = null,
    val confidence: Double? = null,
    val minimumConfidence: Double? = null
) {

    init {
        require(
            confidence == null ||
                    confidence in 0.0..1.0
        ) {
            "confidence must be between 0.0 and 1.0."
        }

        require(
            minimumConfidence == null ||
                    minimumConfidence in 0.0..1.0
        ) {
            "minimumConfidence must be between 0.0 and 1.0."
        }
    }
}

/**
 * Fachliches Ergebnis einer persistierten AI-Match-Entscheidung.
 */
enum class RuntimeKnowledgeGapDecisionOutcome {

    MATCH,

    NO_MATCH,

    AMBIGUOUS
}

/**
 * Normalisiertes Ergebnis der deterministischen Match-Validierung.
 */
data class RuntimeKnowledgeGapValidation(
    val accepted: Boolean,
    val reason: String? = null
)