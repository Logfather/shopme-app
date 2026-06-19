package de.shopme.tools.knowledge.pollinator

class PollinatorClassifier {

    fun classify(
        score: PollinatorScore?
    ): PollinatorLevel {

        if (score == null) {
            return PollinatorLevel.MEDIUM
        }

        return when {

            score.score < 20 ->
                PollinatorLevel.VERY_LOW

            score.score < 40 ->
                PollinatorLevel.LOW

            score.score < 60 ->
                PollinatorLevel.MEDIUM

            score.score < 80 ->
                PollinatorLevel.HIGH

            else ->
                PollinatorLevel.VERY_HIGH

        }

    }

}