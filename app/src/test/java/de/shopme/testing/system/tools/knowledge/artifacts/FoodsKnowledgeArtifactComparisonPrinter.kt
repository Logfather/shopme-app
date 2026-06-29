package de.shopme.testing.system.tools.knowledge.artifacts

class FoodsKnowledgeArtifactComparisonPrinter {

    fun print(
        comparison: FoodsKnowledgeArtifactComparison
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOODS ARTIFACT COMPARISON")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Existing foods.json  : ${comparison.existingCount}")
        println("Generated artifact   : ${comparison.generatedCount}")
        println()
        println("Missing in generated : ${comparison.missingInGenerated.size}")
        println("Missing in existing  : ${comparison.missingInExisting.size}")

        printPreview(
            title = "Top missing in generated",
            values = comparison.missingInGenerated
        )

        printPreview(
            title = "Top missing in existing",
            values = comparison.missingInExisting
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun printPreview(
        title: String,
        values: List<String>
    ) {

        if (values.isEmpty()) {
            return
        }

        println()
        println("$title:")

        values
            .take(20)
            .forEach { value ->
                println("- $value")
            }

    }

}