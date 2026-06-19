package de.shopme.domain.nutrition.scoring

class NutritionCandidateFilter {

    private val milkAlternatives = setOf(
        "hafer",
        "soja",
        "soy",
        "mandel",
        "almond",
        "kokos",
        "coconut",
        "oat"
    )

    private val peanutTerms = setOf(
        "peanut",
        "cacahuète",
        "cacahuetes"
    )

    fun isAllowed(
        query: String,
        candidate: String
    ): Boolean {

        val q = query.lowercase()
        val c = candidate.lowercase()

        if (q == "milch") {

            if (
                milkAlternatives.any {
                    c.contains(it)
                }
            ) {
                return false
            }
        }

        if (q == "butter") {

            if (
                peanutTerms.any {
                    c.contains(it)
                }
            ) {
                return false
            }
        }

        return true
    }
}