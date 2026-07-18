package de.shopme.tools.knowledge.mapping.catalog

data class CatalogServerKnowledgeMappings(
    val version: Int,
    val mappings: List<CatalogServerKnowledgeMapping>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        requireNoDuplicateMappings()
        requireMappingsAreDeterministicallyOrdered()
    }


    private fun requireNoDuplicateMappings() {

        val duplicateCatalogKeys =
            mappings
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate catalog server knowledge mappings: " +
                    duplicateCatalogKeys.sorted()
        }
    }


    private fun requireMappingsAreDeterministicallyOrdered() {

        require(
            mappings ==
                    mappings.sortedWith(MAPPING_ORDER)
        ) {
            "mappings must be ordered by catalogKey and serverKey"
        }
    }


    companion object {

        const val CURRENT_VERSION =
            1

        val MAPPING_ORDER:
                Comparator<CatalogServerKnowledgeMapping> =
            compareBy<CatalogServerKnowledgeMapping> {
                it.catalogKey
            }.thenBy {
                it.serverKey
            }
    }
}