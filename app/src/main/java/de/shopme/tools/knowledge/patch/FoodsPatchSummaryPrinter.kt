package de.shopme.tools.knowledge.patch

class FoodsPatchSummaryPrinter {

    fun print(
        result: FoodsPatchApplyResult
    ) {

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOODS PATCH APPLY REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
        println("Candidates before : ${result.stats.candidateCountBefore}")
        println("Patch entries     : ${result.stats.patchEntryCount}")
        println("Candidates after  : ${result.stats.candidateCountAfter}")
        println()
        println("Validation valid  : ${result.compileResult.validationResult.isValid}")
        println("Validation issues : ${result.compileResult.validationResult.issues.size}")

        println()
        println("Diff summary")
        println("------------")
        println("Added        : ${result.diff.stats.addedCount}")
        println("Updated      : ${result.diff.stats.updatedCount}")
        println("Unchanged    : ${result.diff.stats.unchangedCount}")

        println()
        println("Patch diff")
        println("----------")

        result.diff.entries
            .forEach { entry ->

                println(
                    "${entry.operation.name.padEnd(7)} ${entry.canonicalId}"
                )
            }
    }
}