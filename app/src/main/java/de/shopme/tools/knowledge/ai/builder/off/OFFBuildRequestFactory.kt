package de.shopme.tools.knowledge.ai.builder.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.off.loader.OpenFoodFactsDumpReader
import de.shopme.tools.knowledge.off.loader.OpenFoodFactsRawProductParser
import java.io.File

object OFFBuildRequestFactory {

    fun create(
        file: File = File("../data/raw/openfoodfacts/openfoodfacts-products.jsonl.gz"),
        maxRecords: Int? = null
    ): AIKnowledgeBuildRequest {
        val reader = OpenFoodFactsDumpReader()
        val parser = OpenFoodFactsRawProductParser()
        val adapter = OFFAIImportAdapter()

        val products =
            parser.parseLines(
                reader.readLines(
                    file = file,
                    maxRecords = maxRecords
                )
            )

        return adapter.adapt(products)
    }
}