package de.shopme.tools.knowledge.ai.sources.off

data class OFFRawProduct(

    val code: String,

    val productName: String?,

    val genericName: String? = null,

    val brands: String? = null,

    val categories: String? = null,

    val ingredientsText: String? = null,

    val labels: String? = null,

    val countries: String? = null,

    val origins: String? = null,

    val allergens: String? = null,

    val packaging: String? = null,

    val manufacturingPlaces: String? = null,

    val nutritionGradeFr: String? = null,

    val novaGroup: Int? = null,

    val energyKcal100g: Double? = null,

    val fat100g: Double? = null,

    val saturatedFat100g: Double? = null,

    val carbohydrates100g: Double? = null,

    val sugars100g: Double? = null,

    val fiber100g: Double? = null,

    val proteins100g: Double? = null,

    val salt100g: Double? = null,

)