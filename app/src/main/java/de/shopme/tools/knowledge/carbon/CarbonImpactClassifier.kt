package de.shopme.tools.knowledge.carbon

class CarbonImpactClassifier {

    fun classify(
        footprint: CarbonFootprint?
    ): CarbonImpactLevel {

        if (footprint == null) {
            return CarbonImpactLevel.LOW
        }

        return when {

            footprint.kilogramsPerKilogram < 1.0 ->
                CarbonImpactLevel.LOW

            footprint.kilogramsPerKilogram < 5.0 ->
                CarbonImpactLevel.MEDIUM

            footprint.kilogramsPerKilogram < 15.0 ->
                CarbonImpactLevel.HIGH

            else ->
                CarbonImpactLevel.VERY_HIGH

        }

    }

}