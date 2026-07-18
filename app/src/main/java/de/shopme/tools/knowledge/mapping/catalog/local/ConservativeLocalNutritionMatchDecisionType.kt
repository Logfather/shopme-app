package de.shopme.tools.knowledge.mapping.catalog.local

data class ConservativeLocalNutritionMatchResult(
    val catalogKey: String,
    val decisionType:
    ConservativeLocalNutritionMatchDecisionType,
    val selectedServerKey: String?,
    val probability: Double?,
    val autoAcceptThreshold: Double,
    val candidateCount: Int,
    val reason:
    ConservativeLocalNutritionMatchReason
)

enum class ConservativeLocalNutritionMatchDecisionType {

    /**
     * Der lokale Matcher besitzt einen eindeutigen Top-Kandidaten
     * oberhalb der konservativen Auto-Accept-Schwelle.
     *
     * Das Mapping muss trotzdem noch durch den bestehenden
     * deterministischen fachlichen Validator laufen.
     */
    LOCAL_AUTO_ACCEPT,

    /**
     * Kein ausreichend sicherer lokaler Treffer.
     * Der vollständige Request wird unverändert an GPT-5.5 gegeben.
     */
    GPT_5_5_FALLBACK
}

enum class ConservativeLocalNutritionMatchReason {
    UNIQUE_TOP_CANDIDATE_ABOVE_THRESHOLD,
    NO_CANDIDATES,
    TOP_CANDIDATE_BELOW_THRESHOLD,
    AMBIGUOUS_TOP_PROBABILITY
}

data class ConservativeLocalNutritionScoredCandidate(
    val serverKey: String,
    val candidateRank: Int,
    val probability: Double
)