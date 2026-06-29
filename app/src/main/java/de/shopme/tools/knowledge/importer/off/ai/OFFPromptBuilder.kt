package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderRequest

class OFFPromptBuilder(
    private val template: OFFPromptTemplate = OFFPromptTemplate()
){

    fun build(
        batch: OFFAIExtractionBatch
    ): AIProviderRequest {

        return AIProviderRequest(
            systemPrompt = template.systemPrompt(),
            userPrompt = buildUserPrompt(batch)
        )
    }

    private fun buildUserPrompt(
        batch: OFFAIExtractionBatch
    ): String {

        return batch.products.joinToString(
            separator = "\n\n"
        ) { product: OFFAIExtractionInput ->

            buildString {
                appendLine("code=${product.code ?: ""}")
                appendLine("productName=${product.productName ?: ""}")
                appendLine("productNameDe=${product.productNameDe ?: ""}")
                appendLine("brands=${product.brands ?: ""}")
                appendLine("categories=${product.categories ?: ""}")
                appendLine("ingredientsText=${product.ingredientsText ?: ""}")
                appendLine("ingredientsTextDe=${product.ingredientsTextDe ?: ""}")
                appendLine("labels=${product.labels ?: ""}")
                appendLine("countries=${product.countries ?: ""}")
                appendLine("quantity=${product.quantity ?: ""}")
            }
        }
    }
}