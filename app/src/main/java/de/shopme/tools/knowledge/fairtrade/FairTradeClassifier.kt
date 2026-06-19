package de.shopme.tools.knowledge.fairtrade

class FairTradeClassifier {

    fun classify(

        rating: FairTrade?

    ): FairTradeLevel {

        if (rating == null) {

            return FairTradeLevel.UNKNOWN

        }

        return when {

            rating.score >= 0.8 ->
                FairTradeLevel.EXCELLENT

            rating.score >= 0.5 ->
                FairTradeLevel.GOOD

            rating.score >= 0.2 ->
                FairTradeLevel.PARTIAL

            else ->
                FairTradeLevel.NONE

        }

    }

}