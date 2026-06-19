package de.shopme.tools.report

data class FoodKnowledgeCoverageEntry(

    val name: String,

    val covered: Int,

    val total: Int

) {

    val percentage: Double

        get() =

            if (total == 0) {

                0.0

            } else {

                covered.toDouble() * 100.0 / total.toDouble()

            }

}