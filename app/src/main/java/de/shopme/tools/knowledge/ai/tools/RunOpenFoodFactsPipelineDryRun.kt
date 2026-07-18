package de.shopme.tools.knowledge.ai.tools

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipelineFactory
import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFJsonlPreviewReader
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import java.io.File

object RunOpenFoodFactsPipelineDryRun {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {

        val inputFile =
            listOf(
                File(
                    KnowledgeDataDirectories.openFoodFactsPreview,
                    "off-products-preview-50k.jsonl.gz"
                )
            ).firstOrNull { it.exists() }
                ?: error("OFF preview file not found")

        val catalogFile =
            File.createTempFile(
                "off-ai-dry-run-catalog",
                ".json"
            )

        fun printKnowledgeSamples(
            catalog: List<CatalogItem>
        ) {

            println()
            println("Catalog Knowledge Samples")
            println("-------------------------")

            catalog
                .filter { item ->
                    item.knowledge?.nutrition != null ||
                            item.knowledge?.ingredients != null ||
                            item.knowledge?.taxonomy != null
                }
                .take(5)
                .forEach { item ->

                    println(item.normalized)

                    item.knowledge?.nutrition?.let {
                        println("  nutrition   : ${it.reference} (${it.source})")
                    }

                    item.knowledge?.ingredients?.let {
                        println("  ingredients : ${it.reference} (${it.source})")
                    }

                    item.knowledge?.taxonomy?.let {
                        println("  taxonomy    : ${it.reference} (${it.source})")
                    }

                    println()
                }
        }

        fun printKnowledgeCoverage(
            catalog: List<CatalogItem>
        ) {

            println()
            println("Catalog Knowledge Coverage")
            println("--------------------------")

            println("Nutrition       : ${catalog.count { it.knowledge?.nutrition != null }}")
            println("Ingredients     : ${catalog.count { it.knowledge?.ingredients != null }}")
            println("Taxonomy        : ${catalog.count { it.knowledge?.taxonomy != null }}")
            println("Allergens       : ${catalog.count { it.knowledge?.allergens != null }}")
            println("Glycemic        : ${catalog.count { it.knowledge?.glycemicIndex != null }}")
            println("Carbon          : ${catalog.count { it.knowledge?.carbon != null }}")
            println("Water           : ${catalog.count { it.knowledge?.water != null }}")
            println("Water Stress    : ${catalog.count { it.knowledge?.waterStress != null }}")
            println("Seasonality     : ${catalog.count { it.knowledge?.seasonality != null }}")
            println("Packaging       : ${catalog.count { it.knowledge?.packaging != null }}")
            println("Fairtrade       : ${catalog.count { it.knowledge?.fairTrade != null }}")
            println("Animal Welfare  : ${catalog.count { it.knowledge?.animalWelfare != null }}")
            println("Biodiversity    : ${catalog.count { it.knowledge?.biodiversity != null }}")
            println("Pollinator      : ${catalog.count { it.knowledge?.pollinator != null }}")
            println("Locality        : ${catalog.count { it.knowledge?.locality != null }}")
            println("Food Miles      : ${catalog.count { it.knowledge?.foodMiles != null }}")
            println("Production      : ${catalog.count { it.knowledge?.production != null }}")
            println("Processing      : ${catalog.count { it.knowledge?.processing != null }}")
            println("Pesticides      : ${catalog.count { it.knowledge?.pesticides != null }}")
            println("Carbon Impact   : ${catalog.count { it.knowledge?.carbonImpact != null }}")
        }



        try {
//            val limit =
//                args.firstOrNull()?.toIntOrNull() ?: 10

            val limit =
                args.firstOrNull()?.toIntOrNull() ?: 50_000

            catalogFile.writeText("[]")

            println("======================================")
            println("Open Food Facts AI Pipeline Dry Run")
            println("======================================")
            println()

            println("Input:")
            println("  ${inputFile.absolutePath}")

            println("Catalog:")
            println("  ${catalogFile.absolutePath}")

            println("Limit:")
            println("  $limit")
            println()

//            val products =
//                OFFJsonlPreviewReader()
//                    .read(
//                        file = inputFile,
//                        limit = limit
//                    )

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

            println("AI Builder")
            println("----------")
            println("Source:")
            println("  ${request.source.name}")
            println("Inputs:")
            println("  ${request.inputs.size}")
            println()

            request.inputs
                .take(10)
                .forEachIndexed { index, input ->

                    println(
                        "${index + 1}. ${input.sourceId} (${input.fields.size} fields)"
                    )
                }

            val pipeline =
                DefaultAIKnowledgeBuilderPipelineFactory.create(
                    catalogFile = catalogFile
                )

            val result =
                pipeline.run(request)

            val coverage =
                mutableMapOf<KnowledgeDimensionCandidateType, Int>()

            result.candidates
                .flatMap { it.dimensions }
                .forEach { dimension ->

                    coverage.merge(
                        dimension.dimension,
                        1,
                        Int::plus
                    )
                }

            println()
            println("Dimension Coverage")
            println("------------------")

            KnowledgeDimensionCandidateType
                .entries
                .forEach { type ->

                    println(
                        "${type.name.padEnd(20)} ${coverage[type] ?: 0}"
                    )
                }

            val distribution =
                result.candidates
                    .groupingBy {
                        it.dimensions.size
                    }
                    .eachCount()

            println()
            println("Dimensions per Candidate")
            println("------------------------")

            distribution
                .toSortedMap()
                .forEach { (count, products) ->

                    println(
                        "${count.toString().padStart(2)} dimensions : $products"
                    )
                }

            val updatedCatalog: List<CatalogItem> = Gson().fromJson(
                catalogFile.readText(),
                object : TypeToken<List<CatalogItem>>() {}.type
            )

            printKnowledgeSamples(updatedCatalog)

            printKnowledgeCoverage(updatedCatalog)

            println()
            println("Dry run finished.")
            println("Generated candidates:")
            println("  ${result.candidates.size}")
            println()
            println()

//            val updatedCatalog =
//                catalogFile.readText()

//            println()
//            println("Temporary foods.json")
//            println("--------------------")
//            println(updatedCatalog)
//            println("Path : ${catalogFile.absolutePath}")
//            println("Size : ${catalogFile.length()} bytes")
//
//            println("Candidate Summary")
//            println("-----------------")
//            println()

//            result.candidates.forEachIndexed { index, candidate ->
//
//                println("${index + 1}.")
//                println("  Canonical Id : ${candidate.canonicalId}")
//                println("  Aliases      : ${candidate.aliases.size}")
//                println("  Dimensions   : ${candidate.dimensions.size}")
//
//                candidate.dimensions.forEach {
//
//                    println("    - ${it.dimension}")
//                }
//
//                println()
//            }

            println("Catalog file was temporary and is not persisted.")

        } finally {
            catalogFile.delete()
        }
    }
}