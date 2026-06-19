package de.shopme.domain.nutrition.mapping

object NutritionCanonicalMapping {

    private val mappings = mapOf(

        "milch" to "vollmilch",

        "butter" to "deutsche butter",

        "banane" to "banane",

        "apfel" to "apfel",

        "coca cola" to "coca cola",

        "nutella" to "nutella"
    )

    fun normalize(
        query: String
    ): String {

        return mappings[
            query.lowercase().trim()
        ] ?: query
    }
}