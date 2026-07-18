package de.shopme.tools.knowledge.mapping.catalog.report

data class CatalogServerMappingMiss(
    val catalogKey: String,
    val serverKey: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverKey.isNotBlank()) {
            "serverKey must not be blank"
        }
    }


    companion object {

        val ORDER:
                Comparator<CatalogServerMappingMiss> =
            compareBy<CatalogServerMappingMiss> {
                it.catalogKey
            }.thenBy {
                it.serverKey
            }
    }
}