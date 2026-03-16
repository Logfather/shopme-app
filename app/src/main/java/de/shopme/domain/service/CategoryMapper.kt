package de.shopme.domain.service

import de.shopme.domain.catalog.CatalogIndex

class CategoryMapper(
    private val catalogIndex: CatalogIndex
) {

    fun resolve(name: String): String {

        val normalized =
            name.lowercase().trim()

        val item =
            catalogIndex.normalize(normalized)

        return item?.category ?: "Sonstiges"
    }
}