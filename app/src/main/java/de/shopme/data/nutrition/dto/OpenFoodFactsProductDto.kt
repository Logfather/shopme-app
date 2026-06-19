package de.shopme.data.nutrition.dto

data class OpenFoodFactsProductDto(

    val code: String?,

    val product_name: String?,

    val brands: String?,

    val categories: String?,

    val image_url: String?,

    val nutriscore_grade: String?,

    val nutriments: NutrimentsDto?
)