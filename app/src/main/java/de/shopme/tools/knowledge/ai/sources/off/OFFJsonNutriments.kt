package de.shopme.tools.knowledge.ai.sources.off

import com.google.gson.annotations.SerializedName

data class OFFJsonNutriments(

    @SerializedName("energy-kcal_100g")
    val energy_kcal_100g: Double?,

    @SerializedName("energy_100g")
    val energy_100g: Double?,

    @SerializedName("fat_100g")
    val fat_100g: Double?,

    @SerializedName("saturated-fat_100g")
    val saturated_fat_100g: Double?,

    @SerializedName("carbohydrates_100g")
    val carbohydrates_100g: Double?,

    @SerializedName("sugars_100g")
    val sugars_100g: Double?,

    @SerializedName("fiber_100g")
    val fiber_100g: Double?,

    @SerializedName("proteins_100g")
    val proteins_100g: Double?,

    @SerializedName("salt_100g")
    val salt_100g: Double?
) {

    fun energyKcal100g(): Double? {

        return energy_kcal_100g
            ?: energy_100g?.div(4.184)
    }
}