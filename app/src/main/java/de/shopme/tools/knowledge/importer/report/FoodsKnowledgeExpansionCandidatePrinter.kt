package de.shopme.tools.knowledge.foods.importer.report

class FoodsKnowledgeExpansionCandidatePrinter {

    fun print(
        candidates: List<FoodsKnowledgeExpansionCandidate>
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CANONICAL FOODS EXPANSION CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

        candidates.forEach { candidate ->

            println(
                "${candidate.occurrences} x ${candidate.name} -> ${candidate.mappedReference} ${
                    if (candidate.canonicalExists) "(exists)"
                    else "(missing)"
                }"
            )
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}