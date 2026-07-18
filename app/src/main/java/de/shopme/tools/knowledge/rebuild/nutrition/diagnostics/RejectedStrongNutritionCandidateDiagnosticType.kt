package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

enum class RejectedStrongNutritionCandidateDiagnosticType {

    /**
     * Catalog Key und Candidate sind ohne erkennbaren kritischen
     * Unterschied wahrscheinlich als repräsentativ verwendbar.
     */
    LIKELY_REPRESENTATIVE,

    /**
     * Der Candidate ist eine konkretere Ausprägung derselben
     * Produktklasse.
     */
    COMPATIBLE_SPECIALIZATION,

    /**
     * Der Candidate ist allgemeiner als der Catalog Key, bleibt aber
     * in derselben Produktklasse.
     */
    COMPATIBLE_GENERALIZATION,

    /**
     * Der Candidate ergänzt einen vermutlich nicht kritischen
     * geschmacklichen oder stilistischen Modifier.
     */
    ADDITIONAL_NON_CRITICAL_MODIFIER,

    /**
     * Dem Candidate fehlt ein vermutlich nicht kritischer Modifier.
     */
    MISSING_NON_CRITICAL_MODIFIER,

    /**
     * Catalog Key und Candidate enthalten widersprüchliche kritische
     * Eigenschaften.
     */
    CRITICAL_MODIFIER_CONFLICT,

    /**
     * Der Verarbeitungs- oder Konservierungszustand unterscheidet sich.
     */
    PROCESSING_STATE_CONFLICT,

    /**
     * Catalog Key und Candidate bezeichnen unterschiedliche
     * Produktformen.
     */
    PRODUCT_FORM_CONFLICT,

    /**
     * Catalog Key und Candidate gehören unterschiedlichen
     * Produktklassen an.
     */
    DIFFERENT_PRODUCT_CLASS,

    /**
     * Der Candidate beschreibt eine konkrete Marke, Geschmacksrichtung
     * oder Variante, die nicht sicher repräsentativ ist.
     */
    BRAND_OR_VARIANT_MISMATCH,

    /**
     * Die vorhandenen Tokens reichen nicht für eine belastbare
     * automatische Einordnung.
     */
    INSUFFICIENT_SEMANTIC_EVIDENCE
}