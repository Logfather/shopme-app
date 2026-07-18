package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseRawProduct
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSynthesisColumns

class AgribalyseRawProductMapper {

    fun map(record: Map<String, String>): AgribalyseRawProduct =
        AgribalyseRawProduct(
            agbCode = record.value(AgribalyseSynthesisColumns.AGB_CODE),
            ciqualCode = record.value(AgribalyseSynthesisColumns.CIQUAL_CODE),
            foodGroup = record.value(AgribalyseSynthesisColumns.FOOD_GROUP),
            foodSubgroup = record.value(AgribalyseSynthesisColumns.FOOD_SUBGROUP),
            frenchName = record.value(AgribalyseSynthesisColumns.PRODUCT_NAME),
            lciName = record.value(AgribalyseSynthesisColumns.LCI_NAME),

            seasonCode = record.value(AgribalyseSynthesisColumns.SEASON_CODE).toIntOrNull(),
            airTransportCode = record.value(AgribalyseSynthesisColumns.AIR_TRANSPORT).toIntOrNull(),
            delivery = record.value(AgribalyseSynthesisColumns.DELIVERY),
            packagingApproach = record.value(AgribalyseSynthesisColumns.PACKAGING_APPROACH),
            preparation = record.value(AgribalyseSynthesisColumns.PREPARATION),
            dataQualityScore = record.value(AgribalyseSynthesisColumns.DATA_QUALITY).toDoubleOrNull(),

            singleScoreMptPerKg = record.value(AgribalyseSynthesisColumns.SINGLE_SCORE).toDoubleOrNull(),
            carbonKgCo2EqPerKg = record.value(AgribalyseSynthesisColumns.CARBON).toDoubleOrNull(),
            waterM3DeprivationPerKg = record.value(AgribalyseSynthesisColumns.WATER).toDoubleOrNull(),
            landUsePtPerKg = record.value(AgribalyseSynthesisColumns.LAND_USE).toDoubleOrNull(),
            energyMjPerKg = record.value(AgribalyseSynthesisColumns.ENERGY).toDoubleOrNull(),

            biogenicCarbonKgCo2EqPerKg = record.value(AgribalyseSynthesisColumns.BIOGENIC_CARBON).toDoubleOrNull(),
            fossilCarbonKgCo2EqPerKg = record.value(AgribalyseSynthesisColumns.FOSSIL_CARBON).toDoubleOrNull(),
            landUseChangeCarbonKgCo2EqPerKg = record.value(AgribalyseSynthesisColumns.LAND_USE_CARBON).toDoubleOrNull()
        )

    private fun Map<String, String>.value(key: String): String =
        this[key].orEmpty().trim()
}