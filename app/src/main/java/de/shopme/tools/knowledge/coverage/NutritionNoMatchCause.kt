package de.shopme.tools.knowledge.rebuild.nutrition.coverage

enum class NutritionNoMatchCause {

    /**
     * Catalog Key und Top Candidate gehören erkennbar
     * unterschiedlichen Produktklassen an.
     */
    PRODUCT_CLASS_MISMATCH,

    /**
     * Beide Keys beschreiben dieselbe grobe Produktklasse,
     * aber unterschiedliche zubereitete Gerichte oder Varianten.
     */
    PREPARED_MEAL_VARIANT,

    /**
     * Der Top Candidate enthält einen belastbaren Teil der
     * Catalog-Bedeutung, aber nicht den vollständigen Kern.
     */
    PARTIAL_CORE_TOKEN_MATCH,

    /**
     * Ein einzelner, sehr starker Top Candidate wurde dennoch
     * als NO_MATCH abgelehnt.
     */
    STRONG_TOP_CANDIDATE_REJECTED,

    /**
     * Der Top Candidate besitzt eine mittlere Retrieval-Qualität,
     * aber keine hinreichende semantische Gleichheit.
     */
    MODERATE_TOP_CANDIDATE_REJECTED,

    /**
     * Selbst der beste Candidate besitzt nur eine geringe
     * Retrieval-Qualität.
     */
    WEAK_CANDIDATE_SET,

    /**
     * Die Candidate-Liste enthält mehrere unterschiedliche
     * Produktklassen und damit keinen kohärenten Suchraum.
     */
    MIXED_PRODUCT_CLASS_CANDIDATES,

    /**
     * Keine spezifischere deterministische Ursache wurde erkannt.
     */
    UNEXPLAINED
}