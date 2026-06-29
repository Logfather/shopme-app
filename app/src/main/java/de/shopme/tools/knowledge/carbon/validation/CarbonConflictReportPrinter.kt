package de.shopme.tools.knowledge.carbon.validation

class CarbonConflictReportPrinter {

    fun print(
        report: CarbonConflictValidationReport
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("⚠️ CARBON CONFLICT REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

        println(
            "Warnings : ${report.warnings.size}"
        )

        if (report.warnings.isNotEmpty()) {

            println()

            report.warnings.forEach { warning ->

                println(
                    warning.reference
                )

                println(
                    "  Min        : ${warning.minKgCo2ePerKg}"
                )

                println(
                    "  Max        : ${warning.maxKgCo2ePerKg}"
                )

                println(
                    "  Difference : %.2f %%"
                        .format(
                            warning.differencePercent
                        )
                )

                println()
            }
        }

        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}