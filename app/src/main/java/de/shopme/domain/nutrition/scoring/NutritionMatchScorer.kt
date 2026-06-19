package de.shopme.domain.nutrition.scoring

class NutritionMatchScorer {

    fun score(
        query: String,
        candidate: String
    ): Int {

        val queryNormalized =
            query.lowercase().trim()

        val candidateNormalized =
            candidate.lowercase().trim()

        // Perfekter Treffer
        if (queryNormalized == candidateNormalized) {
            return 1000
        }

        val queryTokens =
            queryNormalized.split(
                Regex("\\s+")
            )

        val candidateTokens =
            candidateNormalized.split(
                Regex("\\s+")
            )

        var score = 0

        // --------------------------------------------------
        // Exakte Token-Treffer
        // --------------------------------------------------

        queryTokens.forEach { queryToken ->

            if (candidateTokens.contains(queryToken)) {
                score += 100
            }
        }

        // --------------------------------------------------
        // Prefix-Treffer
        // --------------------------------------------------

        queryTokens.forEach { queryToken ->

            candidateTokens.forEach { candidateToken ->

                if (
                    candidateToken.startsWith(queryToken)
                    && candidateToken != queryToken
                ) {
                    score += 40
                }
            }
        }

        // --------------------------------------------------
        // Zusätzliche Wörter bestrafen
        // --------------------------------------------------

        val extraWords =
            candidateTokens.size -
                    queryTokens.size

        if (extraWords > 0) {
            score -= extraWords * 15
        }

        // --------------------------------------------------
        // Lange Produktnamen bestrafen
        // --------------------------------------------------

        score -=
            (candidateNormalized.length / 5)

        return score
    }
}