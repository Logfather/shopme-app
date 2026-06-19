package de.shopme.tools.knowledge.animalwelfare

class AnimalWelfareClassifier {

    fun classify(

        rating: AnimalWelfare?

    ): AnimalWelfareLevel {

        if (rating == null) {

            return AnimalWelfareLevel.UNKNOWN

        }

        return when {

            rating.score >= 0.8 ->
                AnimalWelfareLevel.EXCELLENT

            rating.score >= 0.5 ->
                AnimalWelfareLevel.GOOD

            rating.score >= 0.2 ->
                AnimalWelfareLevel.ACCEPTABLE

            else ->
                AnimalWelfareLevel.POOR

        }

    }

}