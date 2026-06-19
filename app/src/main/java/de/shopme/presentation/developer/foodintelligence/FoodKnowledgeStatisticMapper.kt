package de.shopme.presentation.developer.foodintelligence

import de.shopme.tools.report.CoverageDimension
import de.shopme.tools.report.FoodKnowledgeCoverageReport
class FoodKnowledgeStatisticMapper {

    fun map(

        report: FoodKnowledgeCoverageReport

    ): List<FoodKnowledgeStatistic> {

        return report.entries.map { entry ->

            val dimension =

                CoverageDimension.entries.first {

                    it.displayName == entry.name

                }

            FoodKnowledgeStatistic(

                dimension = dimension,

                covered = entry.covered,

                total = entry.total

            )

        }

    }

}