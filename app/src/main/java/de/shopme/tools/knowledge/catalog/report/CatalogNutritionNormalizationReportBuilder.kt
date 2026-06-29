package de.shopme.tools.knowledge.catalog.report

import de.shopme.tools.knowledge.catalog.CatalogNameNormalizer

class CatalogNutritionNormalizationReportBuilder(
    private val nameNormalizer: CatalogNameNormalizer =
        CatalogNameNormalizer()
) {

    fun build(
        normalizedCatalog: String
    ): CatalogNutritionNormalizationReport {

        val itemRegex =
            """\{[\s\S]*?}""".toRegex()

        val normalizedRegex =
            """"normalized"\s*:\s*"([^"]+)"""".toRegex()

        val referenceRegex =
            """"reference"\s*:\s*"([^"]+)"""".toRegex()

        val unknownNames =
            mutableMapOf<String, Int>()

        val unknownExamples =
            mutableMapOf<String, MutableList<String>>()

        var total = 0
        var known = 0
        var unknown = 0

        itemRegex
            .findAll(normalizedCatalog)
            .forEach { match ->

                val itemJson =
                    match.value

                val normalizedName =
                    normalizedRegex
                        .find(itemJson)
                        ?.groupValues
                        ?.get(1)
                        ?: return@forEach

                val reference =
                    referenceRegex
                        .find(itemJson)
                        ?.groupValues
                        ?.get(1)
                        ?: "unknown"

                total++

                if (reference == "unknown") {
                    unknown++

                    val reportName =
                        nameNormalizer.normalize(
                            normalizedName
                        )

                    unknownNames[reportName] =
                        unknownNames.getOrDefault(
                            reportName,
                            0
                        ) + 1

                    unknownExamples
                        .getOrPut(reportName) {
                            mutableListOf()
                        }
                        .add(normalizedName)
                } else {
                    known++
                }
            }

        unknownNames
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

                unknownExamples[name]
                    ?.distinct()
                    ?.take(5)
                    ?.forEach { example: String ->

                        println(
                            "   -> $example"
                        )
                    }
            }

        return CatalogNutritionNormalizationReport(
            totalItems = total,
            knownReferences = known,
            unknownReferences = unknown,
            unknownNames = unknownNames
        )

    }
}