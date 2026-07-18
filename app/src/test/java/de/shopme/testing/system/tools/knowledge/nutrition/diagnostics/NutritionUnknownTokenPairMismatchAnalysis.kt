package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionUnknownTokenPairMismatchAnalysis(
    val version: Int,
    val sourceRelationshipCount: Int,
    val sourceTokenPairObservationCount: Int,
    val sourcePrimaryUnknownRelationshipCount: Int,
    val analyzedRelationshipCount: Int,
    val analyzedTokenPairObservationCount: Int,
    val singleTokenPairRelationshipCount: Int,
    val multiTokenRelationshipCount: Int,
    val countsByCatalogToken: Map<String, Int>,
    val countsByServerToken: Map<String, Int>,
    val countsByTokenPair: Map<String, Int>,
    val countsByCatalogTokenKind:
    Map<NutritionUnknownTokenKind, Int>,
    val countsByServerTokenKind:
    Map<NutritionUnknownTokenKind, Int>,
    val countsByCatalogFoodDomainClass:
    Map<FoodDomainTokenClass, Int>,
    val countsByServerFoodDomainClass:
    Map<FoodDomainTokenClass, Int>,
    val countsByFoodDomainClassPair:
    Map<String, Int>,
    val countsByPairProfile:
    Map<NutritionUnknownTokenPairProfile, Int>,
    val countsByCatalogTokenFrequency: Map<Int, Int>,
    val countsByServerTokenFrequency: Map<Int, Int>,
    val topCatalogTokens:
    List<NutritionUnknownTokenCount>,
    val topServerTokens:
    List<NutritionUnknownTokenCount>,
    val topTokenPairs:
    List<NutritionUnknownTokenPairCount>,
    val topTokenPairsByProfile:
    Map<
            NutritionUnknownTokenPairProfile,
            List<NutritionUnknownTokenPairCount>,
            >,
    val entries:
    List<NutritionUnknownTokenPairMismatchEntry>,
)


data class NutritionUnknownTokenPairMismatchEntry(
    val catalogKey: String,
    val serverKey: String,
    val rank: Int,
    val singleTokenPair: Boolean,
    val observations:
    List<NutritionUnknownTokenPairObservation>,
)


data class NutritionUnknownTokenPairObservation(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val normalizedCatalogToken: String,
    val normalizedServerToken: String,
    val catalogTokenKind: NutritionUnknownTokenKind,
    val serverTokenKind: NutritionUnknownTokenKind,
    val catalogFoodDomainClass: FoodDomainTokenClass,
    val serverFoodDomainClass: FoodDomainTokenClass,
    val pairProfile: NutritionUnknownTokenPairProfile,
)


data class NutritionUnknownTokenCount(
    val token: String,
    val count: Int,
)


data class NutritionUnknownTokenPairCount(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val count: Int,
)


enum class NutritionUnknownTokenKind {

    /**
     * Das normalisierte Token besteht ausschließlich aus Ziffern.
     */
    NUMERIC,

    /**
     * Das Token ist ein deterministisch gepflegtes Funktions-,
     * Verbindungs- oder Retrieval-Stopword.
     */
    STOPWORD,

    /**
     * Das Token ist einem bekannten Nutrition- oder Food-Domain-Vokabular
     * zugeordnet.
     */
    KNOWN_DOMAIN_TOKEN,

    /**
     * Das Token ist weder numerisch noch Stopword und befindet sich noch
     * nicht im deterministischen Domain-Vokabular.
     */
    UNKNOWN_DOMAIN_TOKEN,
}

enum class NutritionUnknownTokenPairProfile {

    /**
     * Mindestens eine Seite ist rein numerisch.
     */
    NUMERIC_INVOLVED,

    /**
     * Beide Seiten sind Stopwords.
     */
    BOTH_STOPWORDS,

    /**
     * Nur das Catalog-Token ist ein Stopword.
     */
    CATALOG_STOPWORD,

    /**
     * Nur das Server-Token ist ein Stopword.
     */
    SERVER_STOPWORD,

    /**
     * Beide Seiten sind bekannte Food-Domain-Tokens.
     */
    BOTH_KNOWN_DOMAIN,

    /**
     * Das Catalog-Token ist bekannt, das Server-Token unbekannt.
     */
    CATALOG_KNOWN_SERVER_UNKNOWN,

    /**
     * Das Catalog-Token ist unbekannt, das Server-Token bekannt.
     */
    CATALOG_UNKNOWN_SERVER_KNOWN,

    /**
     * Beide Seiten sind noch unbekannte Domain-Tokens.
     */
    BOTH_UNKNOWN_DOMAIN,
}