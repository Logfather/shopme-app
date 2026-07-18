package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.agribalyse.adapter.AgribalyseAIImportAdapter
import de.shopme.tools.knowledge.agribalyse.loader.AgribalyseExcelReader
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseRawProductMapper
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetLayout
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipeline
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.compiler.catalog.DefaultFileCatalogUpdateWorkflowFactory
import java.io.File

object CreateFoodsJsonFromAgribalyse {

    @JvmStatic
    fun main(args: Array<String>) {
        val outputFile =
            if (args.isNotEmpty()) {
                File(args[0])
            } else {
                File("../data/generated/foods.agribalyse.json")
            }

        outputFile.parentFile?.mkdirs()

        ensureEmptyCatalogExists(outputFile)

        val reader = AgribalyseExcelReader()
        val rawProductMapper = AgribalyseRawProductMapper()
        val adapter = AgribalyseAIImportAdapter()
        val builder = AgribalyseCanonicalCandidateBuilder()

        val records =
            reader.readRecords(
                sheetType = AgribalyseSheetType.SYNTHESIS,
                layout = AgribalyseSheetLayout.synthesis
            )

        val inputs =
            records
                .map(rawProductMapper::map)
                .map(adapter::adapt)

        val pipeline =
            DefaultAIKnowledgeBuilderPipeline(
                builderResolver = DefaultAIKnowledgeBuilderResolver(
                    buildersBySourceType = mapOf(
                        AIKnowledgeSourceType.AGRIBALYSE to builder
                    )
                ),
                catalogUpdateWorkflow =
                    DefaultFileCatalogUpdateWorkflowFactory.create(
                        file = outputFile
                    )
            )

        val result =
            pipeline.run(
                request = AIKnowledgeBuildRequest(
                    source = AIKnowledgeSourceInfo(
                        type = AIKnowledgeSourceType.AGRIBALYSE,
                        name = "agribalyse",
                        version = "3.2"
                    ),
                    inputs = inputs
                )
            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 AGRIBALYSE FOODS BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${result.candidates.size}")
        println("Output     : ${outputFile.absolutePath}")
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