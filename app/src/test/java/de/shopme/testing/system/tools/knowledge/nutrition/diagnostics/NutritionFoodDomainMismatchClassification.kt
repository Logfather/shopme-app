package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionFoodDomainMismatchClassification(
    val version: Int,
    val sourceRelationshipCount: Int,
    val sourceObservationCount: Int,
    val classifiedRelationshipCount: Int,
    val classifiedObservationCount: Int,
    val countsByPrimaryMismatchType:
    Map<NutritionFoodDomainMismatchType, Int>,
    val countsByObservationMismatchType:
    Map<NutritionFoodDomainMismatchType, Int>,
    val countsByDomainClassPair:
    Map<String, Int>,
    val entries:
    List<NutritionFoodDomainMismatchEntry>,
)

data class NutritionFoodDomainMismatchEntry(
    val catalogKey: String,
    val serverKey: String,
    val rank: Int,
    val singleTokenPair: Boolean,
    val primaryMismatchType:
    NutritionFoodDomainMismatchType,
    val observations:
    List<NutritionFoodDomainMismatchObservation>,
)

data class NutritionFoodDomainMismatchObservation(
    val catalogToken: String,
    val serverToken: String,
    val normalizedCatalogToken: String,
    val normalizedServerToken: String,
    val catalogFoodDomainClass:
    FoodDomainTokenClass,
    val serverFoodDomainClass:
    FoodDomainTokenClass,
    val mismatchType:
    NutritionFoodDomainMismatchType,
    val classPairKey: String,
)

enum class NutritionFoodDomainMismatchType {

    DIET_OR_SUBSTITUTE_DIFFERENCE,

    CROSS_DOMAIN_MISMATCH,

    SAME_DOMAIN_DIFFERENT_ENTITY,

    FORM_OR_PROCESSING_DIFFERENCE,

    REGION_OR_STYLE_DIFFERENCE,

    COMPATIBLE_DOMAIN_RELATIONSHIP,

    UNKNOWN_TOKEN_INVOLVED,

    NON_SEMANTIC_TOKEN_DIFFERENCE,

    UNKNOWN,
}