package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipelineFactory
import de.shopme.tools.knowledge.ai.builder.off.OFFBuildRequestFactory
import java.io.File

object CreateFoodsJsonFromOFFChunks {

    @JvmStatic
    fun main(args: Array<String>) {
        val chunksDirectory =
            if (args.isNotEmpty()) {
                File(args[0])
            } else {
                File("../data/generated/openfoodfacts/chunks-10k")
            }

        val outputDirectory =
            if (args.size >= 2) {
                File(args[1])
            } else {
                File("../data/generated/openfoodfacts/catalog")
            }

        outputDirectory.mkdirs()

        val chunkFiles =
            chunksDirectory
                .listFiles { file ->
                    file.isFile &&
                            file.name.startsWith("off-products-") &&
                            file.name.endsWith(".jsonl.gz")
                }
                .orEmpty()
                .sortedBy { it.name }

        require(chunkFiles.isNotEmpty()) {
            "No OFF chunk files found in ${chunksDirectory.absolutePath}"
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CHUNK BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Chunks : ${chunkFiles.size}")
        println("Input  : ${chunksDirectory.absolutePath}")
        println("Output : ${outputDirectory.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        chunkFiles.forEachIndexed { index, chunkFile ->
            val chunkNumber = index + 1

            val outputFile =
                File(
                    outputDirectory,
                    "foods.off.${chunkNumber.toString().padStart(5, '0')}.json"
                )

            ensureEmptyCatalogExists(outputFile)

            println()
            println("Building chunk $chunkNumber/${chunkFiles.size}")
            println("Input : ${chunkFile.absolutePath}")
            println("Output: ${outputFile.absolutePath}")

            val pipeline =
                DefaultAIKnowledgeBuilderPipelineFactory.create(
                    catalogFile = outputFile
                )

            val result =
                pipeline.run(
                    request = OFFBuildRequestFactory.create(
                        file = chunkFile
                    )
                )

            println("Candidates: ${result.candidates.size}")
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF chunk build finished.")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun ensureEmptyCatalogExists(
        outputFile: File
    ) {
        outputFile.parentFile?.mkdirs()

        if (!outputFile.exists()) {
            outputFile.writeText("[]")
        }
    }
}