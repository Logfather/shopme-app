package de.shopme.tools.knowledge.carbon.merge

class CarbonMergeReportPrinter {

    fun print(
        report: CarbonCandidateMergeReport
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON MERGE REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

        println(
            "Total Candidates : ${report.totalCandidates}"
        )

        println(
            "Merged References: ${report.mergedReferences}"
        )

        println(
            "Conflicts        : ${report.conflicts.size}"
        )

        if (report.conflicts.isNotEmpty()) {

            println()

            report.conflicts.forEach { conflict ->

                println(
                    conflict.reference
                )

                conflict.candidates.forEach { candidate ->

                    println(
                        "  ${candidate.source.name.padEnd(16)} ${candidate.kgCo2ePerKg}"
                    )

                }

                println()
            }
        }

        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}