package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.importer.off.OFFProduct

class OFFAIExtractionInputMapper {

    fun map(
        product: OFFProduct
    ): OFFAIExtractionInput {

        return OFFAIExtractionInput(

            code = product.code,

            productName = product.productName,
            productNameDe = product.productNameDe,

            brands = product.brands,
            categories = product.categories,

            ingredientsText = product.ingredientsText,
            ingredientsTextDe = product.ingredientsTextDe,

            labels = product.labels,
            countries = product.countries,
            quantity = product.quantity
        )
    }
}