package de.shopme.tools.knowledge.ai.builder.runtime

import java.io.File

object CreateMultiSourceRuntimeKnowledge {

    @JvmStatic
    fun main(args: Array<String>) {
        val projectRoot: File =
            File(".")
                .canonicalFile
                .let { current ->
                    if (current.name == "app") {
                        requireNotNull(current.parentFile)
                    } else {
                        current
                    }
                }

        val offFile =
            args.getOrNull(0)?.let(::File)
                ?: projectRoot.resolve(
                    "data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz"
                )

        val agribalyseFile =
            args.getOrNull(1)?.let(::File)
                ?: projectRoot.resolve(
                    "data/generated/agribalyse/agribalyse-foods.slim.tsv"
                )

        val outputDir =
            args.getOrNull(2)?.let(::File)
                ?: projectRoot.resolve(
                    "data/generated/runtime"
                )

        val maxOffCandidates =
            args.getOrNull(3)?.toIntOrNull()
                ?: 50_000

        val result =
            MultiSourceRuntimeKnowledgeBuild()
                .build(
                    offFile = offFile,
                    agribalyseFile = agribalyseFile,
                    outputDir = outputDir,
                    maxOffCandidates = maxOffCandidates
                )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("MULTI SOURCE RUNTIME KNOWLEDGE BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF file=${offFile.path}")
        println("Agribalyse file=${agribalyseFile.path}")
        println("Output dir=${outputDir.path}")
        println()
        println("OFF candidates=${result.offCandidateCount}")
        println("Agribalyse candidates=${result.agribalyseCandidateCount}")
        println("Input candidates=${result.inputCandidateCount}")
        println("Normalized=${result.normalizedCandidateCount}")
        println("Merged=${result.mergedCandidateCount}")
        println("Conflicts=${result.conflictCount}")
        println()
        println("Nutrition candidates=${result.nutritionCandidateCount}")
        println("Environmental candidates=${result.environmentalImpactCandidateCount}")
        println("Multi dimension=${result.multiDimensionCandidateCount}")
        println()
        println("Nutrition artifact entries=${result.nutritionArtifactEntryCount}")
        println("Environmental artifact entries=${result.environmentalImpactArtifactEntryCount}")
        println()
        println("Nutrition artifact=${result.nutritionArtifactFile.path}")
        println("Environmental artifact=${result.environmentalImpactArtifactFile.path}")
        println()
        println("BUILD SUCCESSFUL")
    }
}