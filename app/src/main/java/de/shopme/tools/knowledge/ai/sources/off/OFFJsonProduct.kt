package de.shopme.tools.knowledge.ai.sources.off

data class OFFJsonProduct(

    val code: String?,

    val product_name: String?,

    val generic_name: String?,

    val brands: String?,

    val categories: String?,

    val ingredients_text: String?,

    val labels: String?,

    val countries: String?,

    val origins: String?,

    val allergens: String?,

    val packaging: String?,

    val manufacturing_places: String?,

    val nutrition_grade_fr: String?,

    val nova_group: Int?,

    val nutriments: OFFJsonNutriments?
) {

    fun toRawProduct(): OFFRawProduct {

        return OFFRawProduct(
            code = code.orEmpty(),
            productName = product_name,
            genericName = generic_name,
            brands = brands,
            categories = categories,
            ingredientsText = ingredients_text,
            labels = labels,
            countries = countries,
            origins = origins,
            allergens = allergens,
            packaging = packaging,
            manufacturingPlaces = manufacturing_places,
            nutritionGradeFr = nutrition_grade_fr,
            novaGroup = nova_group,
            energyKcal100g = nutriments?.energyKcal100g(),
            fat100g = nutriments?.fat_100g,
            saturatedFat100g = nutriments?.saturated_fat_100g,
            carbohydrates100g = nutriments?.carbohydrates_100g,
            sugars100g = nutriments?.sugars_100g,
            fiber100g = nutriments?.fiber_100g,
            proteins100g = nutriments?.proteins_100g,
            salt100g = nutriments?.salt_100g
        )
    }
}