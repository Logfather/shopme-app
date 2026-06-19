package de.shopme.presentation.developer.foodintelligence

data class CoveragePercentage(

    val covered: Int,

    val total: Int

) {

    val percentage: Double

        get() =

            if (total == 0) {

                0.0

            } else {

                covered.toDouble() / total.toDouble() * 100.0

            }

}