package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class FoodDomainTokenClassification(
    val originalToken: String,
    val normalizedToken: String,
    val tokenClass: FoodDomainTokenClass,
)

enum class FoodDomainTokenClass {

    NUMERIC,

    STOPWORD,

    ANIMAL_SPECIES,

    ANIMAL_PRODUCT_OR_CUT,

    PROCESSED_ANIMAL_PRODUCT,

    PLANT_INGREDIENT,

    GRAIN_OR_LEGUME,

    NUT_SEED_OR_OIL_SOURCE,

    HERB_OR_SPICE,

    DAIRY_PRODUCT,

    DISH_OR_MEAL,

    BAKERY_OR_STARCH_PRODUCT,

    BEVERAGE,

    SWEET_PRODUCT,

    PRODUCT_FORM,

    PREPARATION_OR_PROCESSING,

    COLOR_OR_APPEARANCE,

    REGION_OR_CUISINE,

    STYLE_OR_QUALITY_MODIFIER,

    QUANTITY_OR_SIZE_MODIFIER,

    DIET_OR_SUBSTITUTE,

    PACKAGING_OR_PRESENTATION,

    OTHER_FOOD_DOMAIN,

    UNKNOWN,
}