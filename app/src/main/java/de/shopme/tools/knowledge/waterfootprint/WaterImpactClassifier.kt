package de.shopme.tools.knowledge.waterfootprint

class WaterImpactClassifier {

    fun classify(
        footprint: WaterFootprint?
    ): WaterImpactLevel {

        if (footprint == null) {
            return WaterImpactLevel.LOW
        }

        return when {

            footprint.litersPerKilogram < 500 ->
                WaterImpactLevel.LOW

            footprint.litersPerKilogram < 2000 ->
                WaterImpactLevel.MEDIUM

            footprint.litersPerKilogram < 8000 ->
                WaterImpactLevel.HIGH

            else ->
                WaterImpactLevel.VERY_HIGH

        }

    }

}