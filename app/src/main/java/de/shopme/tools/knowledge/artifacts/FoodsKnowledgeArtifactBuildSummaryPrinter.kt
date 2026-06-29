package de.shopme.tools.knowledge.artifacts

import de.shopme.tools.knowledge.pipeline.KnowledgeCandidateBuildResult

class FoodsKnowledgeArtifactBuildSummaryPrinter {


    fun print(
        buildResult: KnowledgeCandidateBuildResult,
        artifact: FoodsKnowledgeArtifact
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOODS KNOWLEDGE ARTIFACT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        println("Loaded candidates   : ${buildResult.summary.loadedCandidates}")
        println("Valid candidates    : ${buildResult.summary.validCandidates}")
        println("Rejected candidates : ${buildResult.summary.rejectedCandidates}")

        if (buildResult.rejectedCandidates.isNotEmpty()) {

            println()
            println("Top rejected candidates:")

            buildResult.rejectedCandidates
                .take(10)
                .forEach { rejected ->

                    println(
                        "- ${rejected.candidate.canonicalId}"
                    )

                    rejected.validationErrors
                        .forEach { error ->
                            println("    • $error")
                        }
                }
        }

        println()

        println("Artifact entries    : ${artifact.candidates.size}")

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

}