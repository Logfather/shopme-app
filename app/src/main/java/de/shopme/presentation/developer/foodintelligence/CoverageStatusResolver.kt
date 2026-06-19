package de.shopme.presentation.developer.foodintelligence


object CoverageStatusResolver {

    fun resolve(

        percentage: Double

    ): CoverageStatus {

        return when {

            percentage >= 95.0 ->

                CoverageStatus.GREEN

            percentage >= 50.0 ->

                CoverageStatus.YELLOW

            percentage >= 20.0 ->

                CoverageStatus.ORANGE

            else ->

                CoverageStatus.RED

        }

    }

}