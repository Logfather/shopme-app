package de.shopme.tools.knowledge.ai.sources.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput

class OFFAIImportAdapter {

    fun adapt(
        products: List<OFFRawProduct>
    ): AIKnowledgeBuildRequest {

        return AIKnowledgeBuildRequest(
            source = AIKnowledgeSourceInfo(
                type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                name = "Open Food Facts",
                version = null
            ),
            inputs = products.map { product ->
                RawKnowledgeInput(
                    sourceId = product.code,
                    fields = mapOf(
                        "code" to product.code,
                        "name" to product.productName,
                        "genericName" to product.genericName,
                        "brands" to product.brands,
                        "categories" to product.categories,
                        "taxonomy" to product.categories,
                        "ingredients" to product.ingredientsText,
                        "ingredientsText" to product.ingredientsText,
                        "labels" to product.labels,
                        "countries" to product.countries,
                        "nutrition" to nutritionFields(product),
                        "nutritionGradeFr" to product.nutritionGradeFr,
                        "novaGroup" to product.novaGroup,
                        "allergens" to product.allergens,
                        "packaging" to product.packaging,
                        "production" to product.manufacturingPlaces,
                        "locality" to localityFields(product),
                        "fairtrade" to fairTradeFields(product),
                        "animalWelfare" to animalWelfareFields(product),
                        "processing" to product.novaGroup,
                        "allergens" to product.allergens,
                        "packaging" to product.packaging,
                        "production" to product.manufacturingPlaces,
                        "locality" to localityFields(product),
                        "fairtrade" to fairTradeFields(product),
                        "animalWelfare" to animalWelfareFields(product),
                        "processing" to product.novaGroup,
                    ).filterValues {
                        it != null
                    }
                )
            }
        )
    }

    private fun nutritionFields(
        product: OFFRawProduct
    ): Map<String, Any?>? {

        val fields =
            mapOf(
                "energyKcal100g" to product.energyKcal100g,
                "fat100g" to product.fat100g,
                "saturatedFat100g" to product.saturatedFat100g,
                "carbohydrates100g" to product.carbohydrates100g,
                "sugars100g" to product.sugars100g,
                "fiber100g" to product.fiber100g,
                "proteins100g" to product.proteins100g,
                "salt100g" to product.salt100g
            ).filterValues {
                it != null
            }

        if (fields.isEmpty()) {
            return null
        }

        return fields
    }

    private fun localityFields(
        product: OFFRawProduct
    ): Map<String, Any?>? {

        val fields =
            mapOf(
                "countries" to product.countries,
                "origins" to product.origins
            ).filterValues {
                it != null
            }

        if (fields.isEmpty()) {
            return null
        }

        return fields
    }

    private fun fairTradeFields(
        product: OFFRawProduct
    ): Map<String, Any?>? {

        val labels =
            product.labels
                ?: return null

        if (!labels.contains("fair", ignoreCase = true) &&
            !labels.contains("trade", ignoreCase = true)
        ) {
            return null
        }

        return mapOf(
            "labels" to labels
        )
    }

    private fun animalWelfareFields(
        product: OFFRawProduct
    ): Map<String, Any?>? {

        val labels =
            product.labels
                ?: return null

        val hasAnimalWelfareHint =
            labels.contains("animal welfare", ignoreCase = true) ||
                    labels.contains("free range", ignoreCase = true) ||
                    labels.contains("pasture", ignoreCase = true) ||
                    labels.contains("organic", ignoreCase = true)

        if (!hasAnimalWelfareHint) {
            return null
        }

        return mapOf(
            "labels" to labels
        )
    }
}