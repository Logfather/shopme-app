package de.shopme.tools.knowledge.carbon.report

class CatalogCarbonGapReportPrinter {

    fun print(
        report: CatalogCarbonGapReport
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CATALOG CARBON GAP REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
        println("Catalog Items        : ${report.catalogItems}")
        println("Carbon Covered       : ${report.carbonCovered}")
        println("Missing Carbon       : ${report.missingCarbon}")
        println("Agribalyse Candidates: ${report.agribalyseCandidates.size}")

        println()

        report.agribalyseCandidates
            .forEach { reference ->
                println(reference)
            }

        println()
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}