package de.shopme.tools.knowledge.agribalyse.report

class AgribalyseMappingReportPrinter {

    fun print(
        report: AgribalyseMappingReport
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 AGRIBALYSE MAPPING REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
        println("Total Rows : ${report.totalRows}")
        println("Mapped     : ${report.mappedRows}")
        println("Unmapped   : ${report.unmappedRows}")

        println()
        println("Mapped references:")
        report.mappedReferences
            .entries
            .sortedBy { it.key }
            .forEach { (reference, count) ->
                println("$count x $reference")
            }

        println()
        println("Top unmapped references:")
        report.unmappedReferences
            .entries
            .sortedByDescending { it.value }
            .take(50)
            .forEach { (reference, count) ->
                println("$count x $reference")
            }

        println()
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}