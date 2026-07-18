package de.shopme.tools.knowledge.mapping.catalog

data class CatalogServerKnowledgeMappingConflictReport(
    val version: Int,
    val conflicts: List<CatalogServerKnowledgeMappingConflict>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        require(
            conflicts ==
                    conflicts.sortedWith(
                        CatalogServerKnowledgeMappingConflict.ORDER
                    )
        ) {
            "conflicts must be deterministically ordered"
        }
    }


    companion object {

        const val CURRENT_VERSION =
            1
    }
}