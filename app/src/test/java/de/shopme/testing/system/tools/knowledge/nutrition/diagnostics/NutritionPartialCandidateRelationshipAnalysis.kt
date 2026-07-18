package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionPartialCandidateRelationshipAnalysis(
    val version: Int,
    val sourceEntryCount: Int,
    val sourceCandidateCount: Int,
    val partialCandidateCount: Int,
    val classifiedCandidateCount: Int,
    val countsByPrimaryRelationshipType:
    Map<NutritionPartialCandidateRelationshipType, Int>,
    val countsByDetectedRelationshipType:
    Map<NutritionPartialCandidateRelationshipType, Int>,
    val countsByCatalogOnlyTokenCount:
    Map<Int, Int>,
    val countsByServerOnlyTokenCount:
    Map<Int, Int>,
    val entries:
    List<NutritionPartialCandidateRelationshipEntry>,
)

data class NutritionPartialCandidateRelationshipEntry(
    val catalogKey: String,
    val partialCandidateCount: Int,
    val relationships:
    List<NutritionPartialCandidateRelationship>,
)

data class NutritionPartialCandidateRelationship(
    val rank: Int,
    val catalogKey: String,
    val serverKey: String,
    val sharedTokens: List<String>,
    val catalogOnlyTokens: List<String>,
    val serverOnlyTokens: List<String>,
    val catalogCoverage: Double,
    val serverCoverage: Double,
    val primaryRelationshipType:
    NutritionPartialCandidateRelationshipType,
    val detectedRelationshipTypes:
    List<NutritionPartialCandidateRelationshipType>,
)

enum class NutritionPartialCandidateRelationshipType {

    /**
     * Singular-/Plural- oder einfache morphologische Variante.
     *
     * Beispiele:
     * apple / apples
     * berry / berries
     */
    MORPHOLOGICAL_VARIANT,

    /**
     * Unterschiedlicher Verarbeitungszustand.
     *
     * Beispiele:
     * fresh / dried
     * raw / cooked
     * frozen / canned
     */
    PROCESSING_STATE_MISMATCH,

    /**
     * Unterschiedliche Produktform.
     *
     * Beispiele:
     * tomato / tomato puree
     * milk / milk powder
     * fruit / fruit juice
     */
    PRODUCT_FORM_MISMATCH,

    /**
     * Unterschiedliche Zubereitungsart.
     *
     * Beispiele:
     * grilled / fried
     * roasted / steamed
     * boiled / baked
     */
    PREPARATION_MISMATCH,

    /**
     * Zusätzliche oder abweichende beschreibende Modifier.
     *
     * Beispiele:
     * organic
     * low fat
     * sweetened
     * unsalted
     */
    MODIFIER_MISMATCH,

    /**
     * Produktklasse oder fachliche Spezialisierung unterscheidet sich.
     *
     * Beispiele:
     * cheese / cream cheese
     * rice / rice pudding
     * yogurt / yogurt drink
     */
    PRODUCT_SPECIALIZATION_MISMATCH,

    /**
     * Mehrere fachliche Differenztypen wurden gleichzeitig erkannt.
     */
    MULTIPLE_MISMATCHES,

    /**
     * Gemeinsame Tokens sind vorhanden, aber die Beziehung kann nicht
     * durch die bekannten deterministischen Regeln klassifiziert werden.
     */
    UNCLASSIFIED_PARTIAL,
}