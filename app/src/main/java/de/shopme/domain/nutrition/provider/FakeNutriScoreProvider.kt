package de.shopme.domain.nutrition.provider

object FakeNutriScoreProvider {

    private val scores = mapOf(

        "apfel" to "A",
        "banane" to "A",

        "vollmilch" to "B",

        "butter" to "D",

        "nutella" to "E",

        "coca_cola" to "E"
    )

    fun getScore(
        nutritionReference: String?
    ): String? {

        if (nutritionReference == null) {
            return null
        }

        return scores[nutritionReference]
    }
}