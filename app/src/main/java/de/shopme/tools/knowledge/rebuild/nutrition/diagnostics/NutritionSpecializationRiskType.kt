package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

enum class NutritionSpecializationRiskType {

    /**
     * Der Candidate verändert gezielt die Nährwertformulierung,
     * beispielsweise durch Protein-Anreicherung.
     */
    NUTRITION_FORMULATION,

    /**
     * Der Candidate ergänzt oder verändert einen prägenden
     * Rohstoff beziehungsweise eine Hauptzutat.
     */
    INGREDIENT_OR_SUBSTRATE,

    /**
     * Der Candidate legt eine konkrete Art, Sorte oder Unterklasse
     * fest, obwohl der Catalog Key allgemeiner ist.
     */
    SPECIES_OR_SUBTYPE,

    /**
     * Der Candidate ergänzt eine konkrete Geschmacksrichtung.
     */
    FLAVOR,

    /**
     * Der Candidate bezeichnet eine regionale, traditionelle oder
     * anderweitig eigenständige Produktidentität.
     */
    REGIONAL_OR_PRODUCT_IDENTITY,

    /**
     * Der Candidate ergänzt eine konkrete Produktform oder Geometrie.
     */
    PRODUCT_FORM,

    /**
     * Der Candidate ergänzt eine Verarbeitungsausprägung, die das
     * Nutrition-Profil beeinflussen kann.
     */
    PROCESSING_METHOD,

    /**
     * Der zusätzliche Modifier ist für Nutrition mit hoher
     * Wahrscheinlichkeit nicht identitätsverändernd.
     */
    NON_CRITICAL_STYLE,

    /**
     * Das zusätzliche Token ist noch keiner belastbaren Gruppe
     * zugeordnet.
     */
    UNKNOWN_ADDITIONAL_TOKEN
}