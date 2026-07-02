package de.shopme.tools.knowledge.ai.tools

import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFJsonlPreviewReader
import java.io.File

object CreateOpenFoodFactsAIBuildRequest {

    @JvmStatic
    fun main(args: Array<String>) {

        val inputFile = File(
            "data/preview/off-products-preview"
        )

        val limit =
            args.firstOrNull()?.toIntOrNull() ?: 100

        println("======================================")
        println("Open Food Facts AI Request Builder")
        println("======================================")
        println()

        println("Input:")
        println("  ${inputFile.absolutePath}")
        println("Limit:")
        println("  $limit")
        println()

        val products =
            OFFJsonlPreviewReader()
                .read(
                    file = inputFile,
                    limit = limit
                )

        println("Loaded products:")
        println("  ${products.size}")
        println()

        val request =
            OFFAIImportAdapter()
                .adapt(products)

        println("AI request created")
        println("Source:")
        println("  ${request.source.name}")
        println("Inputs:")
        println("  ${request.inputs.size}")

        request.inputs
            .take(10)
            .forEachIndexed { index, input ->

                println(
                    "${index + 1}. ${input.sourceId} (${input.fields.size} fields)"
                )
            }
    }
}