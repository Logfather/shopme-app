package de.shopme.tools.knowledge.runtime

import de.shopme.tools.knowledge.ai.builder.runtime.MultiSourceRuntimeKnowledgeBuild
import java.io.File

object RunFullMultiSourceRuntimeKnowledgeBuild {

    @JvmStatic
    fun main(args: Array<String>) {

        val result =
            MultiSourceRuntimeKnowledgeBuild()
                .build(
                    offFile =
                        File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz"),

                    agribalyseFile =
                        File("../data/generated/agribalyse/agribalyse-foods.slim.tsv"),

                    outputDir =
                        File("../data/generated/runtime"),

                    maxOffCandidates =
                        null // FULL DATASET
                )

        println(
            """
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            FULL MULTI SOURCE BUILD DONE
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            OFF candidates=${result.offCandidateCount}
            Input=${result.inputCandidateCount}
            Normalized=${result.normalizedCandidateCount}
            Merged=${result.mergedCandidateCount}
            Conflicts=${result.conflictCount}
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            """.trimIndent()
        )
    }
}