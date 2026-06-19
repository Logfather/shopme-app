package de.shopme.presentation.developer.foodintelligence

import de.shopme.tools.report.CoverageDimension

data class FoodKnowledgeStatistic(

    val dimension: CoverageDimension,

    val covered: Int,

    val total: Int

) {

    val name: String

        get() =

            dimension.displayName

    val percentage: Double

        get() =

            if (total == 0) 0.0

            else covered.toDouble() * 100.0 / total.toDouble()

}