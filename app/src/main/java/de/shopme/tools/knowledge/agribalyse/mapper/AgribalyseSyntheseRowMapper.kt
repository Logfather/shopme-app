package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSyntheseRow

class AgribalyseSyntheseRowMapper(

    private val productColumn: String,

    private val carbonColumn: String

) {

    fun map(
        row: Map<String, String>
    ): AgribalyseSyntheseRow {

        return AgribalyseSyntheseRow(

            productName =

                row[productColumn]
                    ?.trim()
                    .orEmpty(),

            climateChangeKgCo2ePerKg =

                row[carbonColumn]
                    ?.replace(",", ".")
                    ?.toDoubleOrNull()
                    ?: 0.0

        )
    }
}