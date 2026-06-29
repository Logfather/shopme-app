package de.shopme.tools.knowledge.foods.importer.report

class FoodsKnowledgeMatchPrinter {

    fun print(
        report: FoodsKnowledgeMatchReport,
        limit: Int = 30
    ) {

        val matchRate =
            if (report.incomingFoods == 0) {
                0.0
            } else {
                report.matchedFoods.toDouble() /
                        report.incomingFoods.toDouble() *
                        100.0
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CANONICAL FOOD MATCH REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
        println("Incoming Foods : ${report.incomingFoods}")
        println("Matched        : ${report.matchedFoods}")
        println("Unmatched      : ${report.unmatchedFoods}")
        println("Match Rate     : ${"%.2f".format(matchRate)} %")
        println()

        if (report.unmatchedOccurrences.isNotEmpty()) {

            println("Top unmatched:")
            println()

            report.unmatchedOccurrences
                .entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }
                        .thenBy { it.key }
                )
                .take(limit)
                .forEach { entry ->

                    println(
                        "${entry.value} x ${entry.key}"
                    )
                }

            println()
        }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}