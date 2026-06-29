package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.nutrition.NutritionFacts

class OFFNutritionFactsMapper {

    fun map(
        source: OFFMinimalProductSource
    ): NutritionFacts? {
        val json =
            source.nutrimentsJson ?: return null

        return NutritionFacts(
            calories = extractDouble(json, "energy-kcal_100g")
                ?: extractDouble(json, "energy_100g")
                ?: return null,
            protein = extractDouble(json, "proteins_100g") ?: 0.0,
            fat = extractDouble(json, "fat_100g") ?: 0.0,
            saturatedFat = extractDouble(json, "saturated-fat_100g") ?: 0.0,
            carbohydrates = extractDouble(json, "carbohydrates_100g") ?: 0.0,
            sugar = extractDouble(json, "sugars_100g") ?: 0.0,
            fiber = extractDouble(json, "fiber_100g") ?: 0.0,
            salt = extractDouble(json, "salt_100g") ?: 0.0
        )
    }

    private fun extractDouble(
        json: String,
        key: String
    ): Double? {
        val regex =
            """"$key"\s*:\s*(-?[0-9]+(?:\.[0-9]+)?)""".toRegex()

        return regex
            .find(json)
            ?.groupValues
            ?.get(1)
            ?.toDoubleOrNull()
    }
}