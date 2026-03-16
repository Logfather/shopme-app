package de.shopme.domain.catalog

data class CatalogMatch(
    val item: String,
    val score: Double
)

class CatalogMatchScorer(
    private val phonetic: PhoneticEncoder
) {

    fun score(
        input: String,
        candidate: String
    ): Double {

        var score = 0.0

        if (input == candidate)
            score += 1.0

        if (candidate.startsWith(input))
            score += 0.7

        val similarity =
            FuzzyMatcher.similarity(input, candidate)

        score += similarity * 0.6

        if (
            phonetic.encode(input)
            ==
            phonetic.encode(candidate)
        ) {
            score += 0.5
        }

        return score
    }
}