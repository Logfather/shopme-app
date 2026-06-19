package de.shopme.domain.recommendation

class RecommendationAggregator {

    fun aggregate(
        results: List<RecommendationResult>
    ): RecommendationResult {

        val score =
            if (results.isEmpty()) {

                100

            } else {

                results.sumOf { it.score } / results.size

            }

        return RecommendationResult(

            score = score,

            reasons = results
                .flatMap { it.reasons }
                .distinct(),

            suggestions = results
                .flatMap { it.suggestions }
                .distinct()

        )

    }

}