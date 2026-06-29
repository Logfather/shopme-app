package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput

class OFFAIKnowledgeBuildRequestMapper {

    fun map(
        batch: OFFAIExtractionBatch
    ): AIKnowledgeBuildRequest {

        return AIKnowledgeBuildRequest(

            source = AIKnowledgeSourceInfo(
                name = batch.source,
                version = batch.sourceVersion
            ),

            inputs = batch.products.map { product ->

                RawKnowledgeInput(

                    sourceId = requireNotNull(product.code) {
                        "OFF product is missing code."
                    },

                    fields = mapOf(
                        "productName" to product.productName,
                        "productNameDe" to product.productNameDe,
                        "brands" to product.brands,
                        "categories" to product.categories,
                        "ingredientsText" to product.ingredientsText,
                        "ingredientsTextDe" to product.ingredientsTextDe,
                        "labels" to product.labels,
                        "countries" to product.countries,
                        "quantity" to product.quantity
                    )
                )
            }
        )
    }
}