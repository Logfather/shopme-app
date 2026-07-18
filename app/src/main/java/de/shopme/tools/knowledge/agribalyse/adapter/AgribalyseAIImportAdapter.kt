package de.shopme.tools.knowledge.agribalyse.adapter

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseRawProduct
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput

class AgribalyseAIImportAdapter {

    fun adapt(
        product: AgribalyseRawProduct
    ): RawKnowledgeInput =
        RawKnowledgeInput(
            sourceId = product.agbCode,
            fields = mapOf(
                "source" to "agribalyse",
                "name" to product.frenchName,

                "taxonomy" to listOfNotNull(
                    product.foodGroup.takeIf { it.isNotBlank() },
                    product.foodSubgroup.takeIf { it.isNotBlank() }
                ),

                "carbon" to product.carbonKgCo2EqPerKg,
                "water" to product.waterM3DeprivationPerKg,

                "production" to listOfNotNull(
                    product.delivery.takeIf { it.isNotBlank() },
                    product.packagingApproach.takeIf { it.isNotBlank() },
                    product.preparation.takeIf { it.isNotBlank() }
                ),

                "ciqualCode" to product.ciqualCode,
                "lciName" to product.lciName,
                "dataQualityScore" to product.dataQualityScore,
                "singleScoreMptPerKg" to product.singleScoreMptPerKg,
                "landUsePtPerKg" to product.landUsePtPerKg,
                "energyMjPerKg" to product.energyMjPerKg,
                "biogenicCarbonKgCo2EqPerKg" to product.biogenicCarbonKgCo2EqPerKg,
                "fossilCarbonKgCo2EqPerKg" to product.fossilCarbonKgCo2EqPerKg,
                "landUseChangeCarbonKgCo2EqPerKg" to product.landUseChangeCarbonKgCo2EqPerKg
            ).filterValues { value ->
                when (value) {
                    null -> false
                    is String -> value.isNotBlank()
                    is Collection<*> -> value.isNotEmpty()
                    else -> true
                }
            }
        )
}