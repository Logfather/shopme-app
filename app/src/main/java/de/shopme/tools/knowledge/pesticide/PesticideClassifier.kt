package de.shopme.tools.knowledge.pesticide

class PesticideClassifier {

    fun classify(

        rating: Pesticide?

    ): PesticideLevel {

        if (rating == null) {

            return PesticideLevel.UNKNOWN

        }

        return when {

            rating.score <= 0.2 ->
                PesticideLevel.LOW

            rating.score <= 0.5 ->
                PesticideLevel.MEDIUM

            rating.score <= 0.8 ->
                PesticideLevel.HIGH

            else ->
                PesticideLevel.VERY_HIGH

        }

    }

}