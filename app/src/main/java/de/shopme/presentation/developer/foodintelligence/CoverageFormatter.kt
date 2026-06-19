package de.shopme.presentation.developer.foodintelligence

import java.text.NumberFormat
import java.util.Locale

object CoverageFormatter {

    private val percentFormatter =

        NumberFormat.getNumberInstance(

            Locale.GERMANY

        ).apply {

            minimumFractionDigits = 1

            maximumFractionDigits = 1

        }

    fun format(

        statistic: FoodKnowledgeStatistic

    ): String =

        "${statistic.covered} / ${statistic.total}     ${
            percentFormatter.format(statistic.percentage)
        } %"

}