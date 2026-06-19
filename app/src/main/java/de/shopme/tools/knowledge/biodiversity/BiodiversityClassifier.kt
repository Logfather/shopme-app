package de.shopme.tools.knowledge.biodiversity

class BiodiversityClassifier {

    fun classify(

        score: BiodiversityScore?

    ): BiodiversityLevel {

        if (score == null) {

            return BiodiversityLevel.MEDIUM

        }

        return when {

            score.score < 20.0 ->

                BiodiversityLevel.VERY_LOW

            score.score < 40.0 ->

                BiodiversityLevel.LOW

            score.score < 60.0 ->

                BiodiversityLevel.MEDIUM

            score.score < 80.0 ->

                BiodiversityLevel.HIGH

            else ->

                BiodiversityLevel.VERY_HIGH

        }

    }

}