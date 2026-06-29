package de.shopme.tools.knowledge.catalog.report

import de.shopme.tools.knowledge.catalog.CatalogNutritionUnknownCategoryClassifier
import kotlin.math.roundToInt

class CatalogNutritionNormalizationReportPrinter {

    fun print(
        report: CatalogNutritionNormalizationReport
    ) {

        println()

        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )

        println(
            "🧠 CATALOG NUTRITION REPORT"
        )

        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )

        println()

        println(
            "Total Items : ${report.totalItems}"
        )

        println(
            "Known       : ${report.knownReferences}"
        )

        println(
            "Unknown     : ${report.unknownReferences}"
        )

        println()

        println(
            "Coverage    : ${
                (report.coveragePercent * 100)
                    .roundToInt() / 100.0
            } %"
        )

        println()
        println("Top unknown normalized names:")

        report.unknownNames
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> {
                    it.value
                }.thenBy {
                    it.key
                }
            )
            .take(100)
            .forEach { (name, count) ->

                println(
                    "$count x $name"
                )

            }

        val categoryClassifier =
            CatalogNutritionUnknownCategoryClassifier()

        println()
        println("Unknowns by category:")
        println()

        report.unknownNames
            .entries
            .groupBy {
                categoryClassifier.classify(it.key)
            }
            .toSortedMap(
                compareBy {
                    it.name
                }
            )
            .forEach { (category, entries) ->

                println(category.name)
                println("-".repeat(category.name.length))

                entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> {
                            it.value
                        }.thenBy {
                            it.key
                        }
                    )
                    .forEach { (name, count) ->

                        println(
                            "$count x $name"
                        )

                    }

                println()

            }

        println()

        println(
            "🏁 FINISHED"
        )

        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )

        println()
    }
}