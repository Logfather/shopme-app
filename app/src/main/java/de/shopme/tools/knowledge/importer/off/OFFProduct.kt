package de.shopme.tools.knowledge.importer.off

import com.google.gson.annotations.SerializedName

data class OFFProduct(

    val code: String? = null,

    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("product_name_de")
    val productNameDe: String? = null,

    val brands: String? = null,
    val categories: String? = null,

    @SerializedName("ingredients_text")
    val ingredientsText: String? = null,

    @SerializedName("ingredients_text_de")
    val ingredientsTextDe: String? = null,

    val labels: String? = null,
    val countries: String? = null,
    val quantity: String? = null
)