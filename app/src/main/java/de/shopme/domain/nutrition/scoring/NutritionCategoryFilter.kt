package de.shopme.domain.nutrition.scoring

class NutritionCategoryFilter {

    private val fruitQueries = setOf(
        "banane",
        "apfel",
        "birne",
        "orange",
        "zitrone",
        "mandarine",
        "traube",
        "erdbeere",
        "himbeere",
        "blaubeere"
    )

    private val processedFoodTerms = setOf(
        "joghurt",
        "yogurt",
        "drink",
        "saft",
        "juice",
        "riegel",
        "bar",
        "dessert",
        "shake",
        "smoothie",
        "müsli",
        "cereal",
        "chips",
        "kuchen",
        "cake",
        "schokolade",
        "chocolate"
    )

    fun isAllowed(
        query: String,
        candidate: String
    ): Boolean {

        val q = query.lowercase().trim()
        val c = candidate.lowercase()

        if (q in fruitQueries) {

            if (
                processedFoodTerms.any {
                    c.contains(it)
                }
            ) {
                return false
            }
        }

        return true
    }
}